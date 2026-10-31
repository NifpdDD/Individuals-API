package transaction_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.UUID;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NotEnoughMoneyException extends RuntimeException {

    private final UUID transactionId;

    public NotEnoughMoneyException(UUID transactionId, String message) {
        super(message);
        this.transactionId = transactionId;
    }

    public NotEnoughMoneyException( String message) {
        super(message);
        this.transactionId = null;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}
