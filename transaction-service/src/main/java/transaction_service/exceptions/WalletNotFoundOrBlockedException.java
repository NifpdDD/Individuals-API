package transaction_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WalletNotFoundOrBlockedException extends RuntimeException {
    public WalletNotFoundOrBlockedException(String message) {
        super(message);
    }
}
