package transaction_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WalletNotFoundOrBlockedException extends RuntimeException {

    private final UUID transactionId; // Добавили хранение ID транзакции

    public WalletNotFoundOrBlockedException(UUID transactionId, String message) {
        super(message);
        this.transactionId = transactionId;
    }

    public WalletNotFoundOrBlockedException(String message) {
        super(message);
        this.transactionId = null;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}
