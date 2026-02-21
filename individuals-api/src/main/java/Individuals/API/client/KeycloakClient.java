package Individuals.API.client;


import com.example.dto.TokenResponse;
import com.example.dto.UserLoginRequest;
import com.example.dto.UserRegistrationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class KeycloakClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final KeycloakProperties keycloakProperties;

    private final WebClient webClient;

    public KeycloakClient(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.webClient = WebClient.builder().baseUrl(keycloakProperties.getBaseUrl()).build();
    }

    public Mono<TokenResponse> requestToken(UserLoginRequest userLoginRequest) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("username", userLoginRequest.getEmail());
        form.add("password", userLoginRequest.getPassword());

        return webClient.post()
                .uri(keycloakProperties.getTokenEndpointUrl(), keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    public Mono<UserLoginRequest> createUser(UserRegistrationRequest userRegistrationRequest, TokenResponse accessToken) {
        String body = "{\"username\": \"" + userRegistrationRequest.getEmail() +
                "\", \"email\": \"" + userRegistrationRequest.getEmail() +
                "\", \"enabled\": \"true\", " +
                "\"credentials\": [{\"temporary\": false, \"type\": \"password\", \"value\": \"" + userRegistrationRequest.getPassword() + "\"}], " +
                "\"emailVerified\": \"true\", " +
                "\"firstName\": \"" + userRegistrationRequest.getEmail() + "\", " +
                "\"lastName\": \"" + userRegistrationRequest.getEmail() + "\"}";
        webClient.post()
                .uri("/admin/realms/{realm}/users", keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken.getAccessToken())
                .body(BodyInserters.fromValue(
                        body
                ))
                .retrieve();
        return Mono.just(new UserLoginRequest().password(userRegistrationRequest.getPassword()).email(userRegistrationRequest.getEmail()));
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
