package Individuals.API.client;


import com.example.dto.TokenResponse;
import com.example.dto.UserLoginRequest;
import com.example.dto.UserRegistrationRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class KeycloakClient {

    private final KeycloakProperties keycloakProperties;

    private final WebClient webClient;

    public KeycloakClient(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.webClient = WebClient.builder().baseUrl(keycloakProperties.getBaseUrl()).build();
    }

    public Mono<TokenResponse> requestToken(UserLoginRequest userLoginRequest) {
        return webClient.post()
                .uri(keycloakProperties.getTokenEndpointUrl(), keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("username", userLoginRequest.getEmail())
                        .with("password", userLoginRequest.getPassword()))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    public void createUser(UserRegistrationRequest userRegistrationRequest) {
        webClient.post()
                .uri("/admin/realms/{realm}/users", keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        "{\"username\": \"" + userRegistrationRequest.getEmail() +
                                "\", \"email\": \"" + userRegistrationRequest.getEmail() +
                                "\", \"enabled\": \"true\", " +
                                "\"credentials\": [{\"temporary\": false, \"type\": \"password\", \"value\": \"" + userRegistrationRequest.getPassword() + "\"}], " +
                                "\"emailVerified\": \"true\", " +
                                "\"firstName\": \"" + userRegistrationRequest.getEmail() + "\", " +
                                "\"lastName\": \"" + userRegistrationRequest.getEmail() + "\"}"
                ))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return webClient.post()
                .uri(keycloakProperties.getTokenEndpointUrl(), keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(TokenResponse.class);


    }
}
