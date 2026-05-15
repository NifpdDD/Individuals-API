package api.exception.keycloak;

public class ValidationEcxeption extends KeyCloakApiException {
    public ValidationEcxeption(String message) {
        super(401, message);
    }
}
