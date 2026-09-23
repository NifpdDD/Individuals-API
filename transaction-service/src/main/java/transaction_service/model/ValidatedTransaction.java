package transaction_service.model;


import transaction_service.entity.enums.TransactionType;

public interface ValidatedTransaction {
    TransactionType getType();
}
