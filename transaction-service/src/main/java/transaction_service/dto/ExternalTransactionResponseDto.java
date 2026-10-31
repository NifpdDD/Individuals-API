package transaction_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import transaction_service.entity.enums.ExternalTransactionStatus;
import transaction_service.entity.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalTransactionResponseDto {
    private UUID transactionId;
    private ExternalTransactionStatus status;
    private Instant timestamp;
    private BigDecimal amount;
    private String failureReason;
}