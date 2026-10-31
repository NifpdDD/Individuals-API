package transaction_service.scheduler;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import transaction_service.entity.OutboxEvent;
import transaction_service.repository.OutBoxEventRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OutboxScheduler {

    public static final int TIMEOUT = 5;
    private final OutBoxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Tracer tracer;

    // Создаем пул виртуальных потоков (доступно с Java 21)
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Value("${app.outbox.page-size:100}")
    private int pageSize;

    public OutboxScheduler(OutBoxEventRepository outboxRepository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           Tracer tracer) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
    }

    @Scheduled(fixedDelayString = "${app.outbox.scheduler-delay-ms:10000}")
    public void processOutboxEventsWithKafkaTools() {
        List<OutboxEvent> events = outboxRepository.findUnprocessedEventsForUpdate(PageRequest.of(0, pageSize));
        if (events.isEmpty()) {
            return;
        }
        for (OutboxEvent event : events) {
            virtualThreadExecutor.submit(() -> processSingleEvent(event));
        }
    }


    //Прочитать про виртуальные потоки
    private void processSingleEvent(OutboxEvent event) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId", String.valueOf(event.getTransactionId()))) {
            String savedTraceId = event.getTraceId();
            TraceContext remoteContext = tracer.traceContextBuilder()
                    .traceId(savedTraceId)
                    .spanId(tracer.nextSpan().context().spanId())
                    .sampled(true)
                    .build();
            Span outboxSpan = tracer.spanBuilder()
                    .name("outbox-kafka-send")
                    .setParent(remoteContext)
                    .start();
            try (Tracer.SpanInScope ws = tracer.withSpan(outboxSpan)) {
                kafkaTemplate.send(event.getTopic(), event.getUserId().toString(), event.getPayload()).get(TIMEOUT, TimeUnit.SECONDS);;
                outboxRepository.markAsProcessed(List.of(event.getUuid()));
                log.info("Event [{}] successfully sent to Kafka for aggregate [{}]", event.getUuid(), event.getUserId());
            } catch (TimeoutException toEx) {
                outboxSpan.error(toEx);
                log.error("Kafka broker timeout for aggregateId [{}]. Event will be retried in next batch.", event.getUserId());
            } catch (Exception ex) {
                outboxSpan.error(ex);
                log.error("Kafka rejected event with aggregateId [{}]: {}", event.getUserId(), ex.getMessage());
            } finally {
                outboxSpan.end();
            }
        }
    }
}
