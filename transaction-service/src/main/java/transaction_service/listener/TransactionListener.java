package transaction_service.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import transaction_service.dto.ExternalTransactionResponseDto;
import transaction_service.dto.TransactionEventDto;
import transaction_service.service.transaction.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.transactions-out}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ExternalTransactionResponseDto dto) {
        log.info("Received transaction response for ID: {}", dto.getTransactionId());
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId", dto.getTransactionId().toString())) {
            transactionService.processTransactionUpdate(dto);
        }
    }
}
