package Individuals.API.exception.keycloak;

import lombok.Getter;

@Getter
public abstract class KeyCloakApiException extends RuntimeException {

    private int statusCode;

    public KeyCloakApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

}
