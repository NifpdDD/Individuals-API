package api.exception.keycloak;

public class UserNotFoundException extends KeyCloakApiException {
    public UserNotFoundException(String message) {
        super(404, message);
    }
}