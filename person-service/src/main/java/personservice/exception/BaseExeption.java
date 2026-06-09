package personservice.exception;

public class BaseExeption extends RuntimeException {
    public BaseExeption(String message) {
        super(message);
    }

    public BaseExeption(String message, Object... args) {
        super(String.format(message,args));
    }
}
