package transaction_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import transaction_service.model.TransactionEvent;
import transaction_service.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper; // Спринг подставит его автоматически

    @KafkaListener(
            topics = "all_transactions_topic_out",
            containerFactory = "kafkaListenerContainerFactory"// Смените имя группы для сброса зависшего офсета!
    )
    public void listen(ConsumerRecord<String, String> record) { // Принимаем String!
        String rawJson = record.value();
        if (rawJson == null) return;

        log.info("Сообщение успешно долетело до метода! Ключ: {}", record.key());

        try {
            // Парсим строку руками
            TransactionEvent event = objectMapper.readValue(rawJson, TransactionEvent.class);
            transactionService.processTransactionUpdate(event);
        } catch (Exception e) {
            log.error("Ошибка парсинга JSON: {}", e.getMessage());
        }
    }
}
