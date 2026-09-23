package transaction_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import transaction_service.entity.OutboxEvent;
import transaction_service.repository.OutBoxEventRepository;
import transaction_service.util.DateTimeUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    public static final String WITHDRAWAL_TRANSACTIONS_TOPIC_IN = "withdrawal_transactions_topic_in";
    public static final String DEPOSIT_TRANSACTIONS_TOPIC_IN = "deposit_transactions_topic_in";
    private final OutBoxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final DateTimeUtil dateTimeUtil;

    public void saveDepositEvent(UUID transactionId, UUID userUuid, UUID walletId, BigDecimal amount, String currency, Instant timestamp) {
            Map<String, Object> messageMap = Map.of(
                    "transactionId", transactionId.toString(),
                    "userId",  userUuid.toString(),
                    "walletId", walletId.toString(),
                    "amount", amount,
                    "currency", currency,
                    "timestamp", timestamp.toString()
            );
            JsonNode jsonNodePayload = objectMapper.valueToTree(messageMap);
            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(walletId);
            event.setTopic(DEPOSIT_TRANSACTIONS_TOPIC_IN);
            event.setPayload(jsonNodePayload);
            event.setCreatedAt(dateTimeUtil.now());
            event.setProcessed(false);
            outboxRepository.save(event);
    }

    public void saveWithdrawalEvent(UUID transactionId, UUID userUuid, UUID walletId, BigDecimal amount, String currency, String destination, Instant timestamp) {
            Map<String, Object> messageMap = Map.of(
                    "transactionId", transactionId.toString(),
                    "userId", userUuid.toString(),
                    "walletId", walletId.toString(),
                    "amount", amount,
                    "currency", currency,
                    "destination", destination,
                    "timestamp", timestamp.toString()
            );
            com.fasterxml.jackson.databind.JsonNode jsonNodePayload = objectMapper.valueToTree(messageMap);
            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(transactionId);
            event.setTopic(WITHDRAWAL_TRANSACTIONS_TOPIC_IN);
            event.setPayload(jsonNodePayload);
            event.setCreatedAt(Instant.now());
            event.setProcessed(false);
            outboxRepository.save(event);
    }
}