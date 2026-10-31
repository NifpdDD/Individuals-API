package transaction_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import transaction_service.entity.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class TransactionEventDto {
    private UUID transactionId;
    private UUID userId;
    private UUID walletId;
    private BigDecimal amount;
    private String currency;
    private String destination;
    private Instant timestamp;
}