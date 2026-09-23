package transaction_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import transaction_service.entity.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private UUID transactionId;
    private TransactionStatus status;
    private BigDecimal amount;
    private Instant timestamp;
    private String failureReason;
}
