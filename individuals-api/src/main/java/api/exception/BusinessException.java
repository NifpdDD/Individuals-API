package api.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private int statusCode;

    public BusinessException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

}
