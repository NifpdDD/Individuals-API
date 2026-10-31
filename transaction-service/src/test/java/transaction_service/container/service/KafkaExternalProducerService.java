package transaction_service.container.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import transaction_service.dto.ExternalTransactionResponseDto;
import transaction_service.entity.enums.ExternalTransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaExternalProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.transactions-out:transactions-out-topic}")
    private String transactionsOutTopic;

    public void sendKafkaCompletedMessage(String transactionId, BigDecimal amount) {
        ExternalTransactionResponseDto message = new ExternalTransactionResponseDto();
        message.setTransactionId(UUID.fromString(transactionId));
        message.setStatus(ExternalTransactionStatus.COMPLETED);
        message.setAmount(amount);
        message.setTimestamp(java.time.Instant.parse("2025-06-18T12:03:12Z"));

        try {
            kafkaTemplate.send(transactionsOutTopic, transactionId, message).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send COMPLETED message to Kafka", e);
        }
    }

    public void sendKafkaFailedMessage(String transactionId) {
        ExternalTransactionResponseDto message = new ExternalTransactionResponseDto();
        message.setTransactionId(UUID.fromString(transactionId));
        message.setStatus(ExternalTransactionStatus.FAILED);
        message.setFailureReason("INSUFFICIENT_FUNDS_EXTERNAL");
        message.setTimestamp(java.time.Instant.parse("2025-06-18T12:17:00Z"));

        try {
            kafkaTemplate.send(transactionsOutTopic, transactionId, message).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send FAILED message to Kafka", e);
        }
    }
}
