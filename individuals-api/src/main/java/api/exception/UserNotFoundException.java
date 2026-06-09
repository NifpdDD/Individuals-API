package api.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String message) {
        super(404, message);
    }
}