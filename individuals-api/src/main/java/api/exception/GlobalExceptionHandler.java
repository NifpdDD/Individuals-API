package api.exception;

import api.exception.keycloak.KeyCloakApiException;
import individuals.api.individuals.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(WebExchangeBindException ex) {
        return Mono.fromCallable(() -> ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(err -> {
                    if (err instanceof FieldError fieldError) {
                        return String.format("Поле %s: %s", fieldError.getField(), err.getDefaultMessage());
                    }
                    return err.getDefaultMessage();
                })
                .orElse("Unknown validation error")
        ).map(errorMessage -> {
            ErrorResponse errorResponse = new ErrorResponse()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(errorMessage);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        });
    }

    @ExceptionHandler(KeyCloakApiException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleApiExection(KeyCloakApiException ex) {
        log.error("Keycloak API Error: Status [{}], Message [{}]",
                ex.getStatusCode(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse()
                .status(ex.getStatusCode())
                .error(ex.getMessage());

        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .body(errorResponse));
    }
}