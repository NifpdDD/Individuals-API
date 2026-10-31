package transaction_service.dto;

import transaction_service.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record ValidatedWithdrawalDto(
    TransactionType type,
    UUID walletUuid, 
    BigDecimal amount, 
    String currency,
    UUID userUuid,
    BigDecimal fee
) implements ValidatedTransaction {
    @Override
    public TransactionType getType() { return type; }
}
