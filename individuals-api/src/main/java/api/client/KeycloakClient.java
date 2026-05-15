package api.client;


import api.exception.keycloak.UnauthorizedException;
import api.exception.keycloak.UserAlreadyExistsException;
import api.mapper.KeycloakMapper;
import individuals.api.individuals.dto.TokenResponse;
import individuals.api.individuals.dto.UserLoginRequest;
import individuals.api.individuals.dto.UserRegistrationRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
    private final KeycloakMapper keycloakMapper;
    private final WebClient webClient;

    public KeycloakClient(KeycloakProperties keycloakProperties, KeycloakMapper keycloakMapper) {
        this.keycloakProperties = keycloakProperties;
        this.keycloakMapper = keycloakMapper;
        this.webClient = WebClient.builder().baseUrl(keycloakProperties.getServerUrl()).build();
    }

    @WithSpan("keycloakClient.login")
    public Mono<TokenResponse> login(UserLoginRequest userLoginRequest) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("username", userLoginRequest.getEmail());
        form.add("password", userLoginRequest.getPassword());


        return webClient.post()
                .uri(keycloakProperties.getTokenEndpointUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        response -> Mono.error(new UnauthorizedException("Неверный логин или пароль")))
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.adminLogin")
    public Mono<TokenResponse> adminLogin() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getAdminClientId());
        form.add("username", keycloakProperties.getAdminEmail());
        form.add("password", keycloakProperties.getAdminPassword());


        return webClient.post()
                .uri(keycloakProperties.getTokenEndpointUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        response -> Mono.error(new UnauthorizedException("Неверный логин или пароль")))
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.createUser")
    public Mono<Void> createUser(UserRegistrationRequest userRegistrationRequest, TokenResponse accessToken) {
        var body = keycloakMapper.toCreateUserRequest(userRegistrationRequest);
        return webClient.post()
                .uri(keycloakProperties.getRealmUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken.getAccessToken())
                .bodyValue(body)
                .retrieve().onStatus(status -> status.value() == 409,
                        response -> Mono.error(new UserAlreadyExistsException("Пользователь с таким email уже существует")))
                .toBodilessEntity().doOnError(x -> log.error("Пользователь с email {} уже существует", userRegistrationRequest.getEmail())).then();
    }

    @WithSpan("keycloakClient.refreshToken")
    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return webClient.post()
                .uri(keycloakProperties.getTokenEndpointUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("refresh_token", refreshToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> Mono.error(new UnauthorizedException("Неверный рефреш токен")))
                .bodyToMono(TokenResponse.class)
                .doOnError(x -> log.error("Неверный рефреш токен"));


    }

}
