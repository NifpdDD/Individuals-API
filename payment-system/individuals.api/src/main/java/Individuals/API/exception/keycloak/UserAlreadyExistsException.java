package Individuals.API.exception.keycloak;

public class UserAlreadyExistsException extends KeyCloakApiException {
    public UserAlreadyExistsException(String message) {
        super(409, message);
    }
}