package transaction_service.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import transaction_service.exceptions.NotEnoughMoneyException;
import transaction_service.exceptions.NotSupportedTypeException;
import transaction_service.exceptions.WalletNotFoundOrBlockedException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String FAILED = "FAILED";

    @ExceptionHandler(WalletNotFoundOrBlockedException.class)
    public ResponseEntity<ErrorResponseDto> handleWalletNotFoundOrBlocked(
            WalletNotFoundOrBlockedException ex) {
        String txId = ex.getTransactionId() != null ? ex.getTransactionId().toString() : "";
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId", txId)) {
            log.warn("Wallet business error: {}", ex.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @ExceptionHandler(NotSupportedTypeException.class)
    public ResponseEntity<ErrorResponseDto> handleNotSupportedType(
            NotSupportedTypeException ex) {
            log.warn("Unsupported operation type error: {}", ex.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotEnoughMoneyException.class)
    public ResponseEntity<ErrorResponseDto> handleNotEnoughMoney(
            NotEnoughMoneyException ex) {
        String txId = ex.getTransactionId() != null ? ex.getTransactionId().toString() : "";
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId", txId)) {
            log.warn("Insufficient funds business error: {}", ex.getMessage());
            return createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        String detailMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Поле '%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        String fullMessage = "Ошибка валидации входящего запроса: " + detailMessage;
        log.warn("Incoming request validation failed: {}", fullMessage);
        return createErrorResponse(HttpStatus.BAD_REQUEST, fullMessage);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = "Некорректный формат JSON или синтаксическая ошибка в теле запроса";
        log.warn("Malformed JSON received: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Параметр '%s' имеет неверный тип данных. Ожидается: %s", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("URL path or query parameter type mismatch: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllUncaughtExceptions(Exception ex) {
        log.error("Internal server unexpected error occurred: ", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера. Пожалуйста, повторите попытку позже.");
    }


    private ResponseEntity<ErrorResponseDto> createErrorResponse(HttpStatus status, String message) {
        ErrorResponseDto response = new ErrorResponseDto(FAILED, message);
        return ResponseEntity.status(status).body(response);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponseDto(
            String status,
            @JsonProperty("message")
            String failMessage
    ) {}
}
