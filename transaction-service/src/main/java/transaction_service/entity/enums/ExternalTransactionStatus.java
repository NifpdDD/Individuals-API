package transaction_service.entity.enums;

public enum ExternalTransactionStatus {

    COMPLETED,
    FAILED;

    public TransactionStatus toTransactionStatus() {
        return switch (this) {
            case COMPLETED -> TransactionStatus.SUCCESS;
            case FAILED -> TransactionStatus.FAILED;
        };
    }
}