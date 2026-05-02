package Individuals.API.exception.keycloak;

public class UnauthorizedException extends KeyCloakApiException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
}