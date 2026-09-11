package transaction_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotSupportedType extends RuntimeException {
    public NotSupportedType(String message) {
        super(message);
    }
}
