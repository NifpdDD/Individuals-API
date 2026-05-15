package api.testcontainer.service;

import api.client.KeycloakProperties;
import individuals.api.individuals.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakApiTestService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;
    private final IndividualApiTestService individualApiTestService;

    public UserRepresentation getUserRepresentation(String email) {
        var users = keycloak.realm(keycloakProperties.getRealm()).users().list();

        for (var user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }

//        throw new ApiException("User not found by email=[ %s ]", email);
        return null;
    }

    public void clear() {
        var users = keycloak.realm(keycloakProperties.getRealm()).users().list();
        for (var user : users) {
            if (!keycloakProperties.getAdminEmail().equals(user.getUsername()))    {
                keycloak.realm(keycloakProperties.getRealm()).users().delete(user.getId());
            }
        }
    }

    public String getAdminAccessToken() {
        return individualApiTestService.login(new UserLoginRequest(keycloakProperties.getAdminEmail(), keycloakProperties.getAdminPassword()))
            .getAccessToken();
    }
}
