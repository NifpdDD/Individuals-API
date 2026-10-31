package transaction_service.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import transaction_service.dto.TransactionEventDto;
import transaction_service.entity.OutboxEvent;
import transaction_service.repository.OutBoxEventRepository;
import transaction_service.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
public class OutboxFactory {

    public static final String TRANSACTIONS_TOPIC_IN = "transactions_topic_in";
    private final OutBoxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final DateTimeUtil dateTimeUtil;

    public void saveEvent(TransactionEventDto dto, String traceId) throws JsonProcessingException {
        String jsonStringPayload = objectMapper.writeValueAsString(dto);
        OutboxEvent event = OutboxEvent.builder()
                .userId(dto.getUserId())
                .topic(TRANSACTIONS_TOPIC_IN)
                .payload(jsonStringPayload)
                .createdAt(dateTimeUtil.now())
                .processed(false)
                .traceId(traceId)
                .transactionId(dto.getTransactionId())
                .build();
        outboxRepository.save(event);
    }
}
