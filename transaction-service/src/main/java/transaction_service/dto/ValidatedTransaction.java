package transaction_service.dto;


import transaction_service.entity.enums.TransactionType;

public interface ValidatedTransaction {
    TransactionType getType();
}
