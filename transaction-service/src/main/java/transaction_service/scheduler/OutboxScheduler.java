package transaction_service.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.entity.OutboxEvent;
import transaction_service.repository.OutBoxEventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutBoxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.scheduler-delay-ms:10000}")
    public void processOutboxEventsWithKafkaTools() {
        List<OutboxEvent> events = outboxRepository.findUnprocessedEvents(PageRequest.of(0, 100));
        if (events.isEmpty()) {
            return;
        }
        List<Future<org.springframework.kafka.support.SendResult<String, Object>>> kafkaFutures = new ArrayList<>(events.size());
        List<UUID> successfullySentUuids = new ArrayList<>(events.size());
        for (OutboxEvent event : events) {
            Future<org.springframework.kafka.support.SendResult<String, Object>> future = 
                    kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload());
            kafkaFutures.add(future);
        }
        kafkaTemplate.flush();
        for (int i = 0; i < events.size(); i++) {
            OutboxEvent event = events.get(i);
            Future<?> future = kafkaFutures.get(i);
            try {
                future.get(); 
                successfullySentUuids.add(event.getUuid());
            } catch (Exception ex) {
                log.error("Kafka rejected event [{}] for topic [{}]: {}", 
                        event.getUuid(), event.getTopic(), ex.getMessage());
            }
        }
        if (!successfullySentUuids.isEmpty()) {
            outboxRepository.markAsProcessed(successfullySentUuids);
            log.info("Kafka native batch completed: {} events confirmed and marked in DB", successfullySentUuids.size());
        }
    }
}
