package transaction_service.dto;

import transaction_service.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record ValidatedDepositDto(
    TransactionType type,
    UUID walletUuid, 
    BigDecimal amount, 
    String currency,
    UUID userUUID,
    BigDecimal fee
) implements ValidatedTransaction {
    @Override
    public TransactionType getType() { return type; }
}
