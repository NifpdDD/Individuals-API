package api.exception;

public class ValidationEcxeption extends BusinessException {
    public ValidationEcxeption(String message) {
        super(401, message);
    }
}
