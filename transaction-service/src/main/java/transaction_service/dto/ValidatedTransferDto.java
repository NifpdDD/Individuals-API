package transaction_service.dto;

import transaction_service.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record ValidatedTransferDto(
    TransactionType type,
    UUID fromUserUuid,
    UUID toUserUuid,
    UUID fromWalletUuid,
    UUID toWalletUuid,
    BigDecimal amount,
    String currency
) implements ValidatedTransaction {
    @Override
    public TransactionType getType() { return type; }
}
