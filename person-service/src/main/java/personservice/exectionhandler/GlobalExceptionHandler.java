package personservice.exectionhandler;

import com.example.person.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import personservice.exception.BusinessException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(WebExchangeBindException ex) {
        var errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(err -> {
                    if (err instanceof FieldError fieldError) {
                        return String.format("Поле %s: %s", fieldError.getField(), err.getDefaultMessage());
                    }
                    return err.getDefaultMessage();
                })
                .orElse("Unknown validation error");
        ErrorResponse errorResponse = new ErrorResponse()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
}

@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleApiExection(BusinessException ex) {
    log.error("Keycloak API Error: Status [{}], Message [{}]",
            ex.getStatusCode(), ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse()
            .status(ex.getStatusCode())
            .error(ex.getMessage());

    return ResponseEntity
            .status(ex.getStatusCode())
            .body(errorResponse);
}
}