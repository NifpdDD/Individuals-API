package Individuals.API.client;


import Individuals.API.exception.keycloak.UnauthorizedException;
import Individuals.API.exception.keycloak.UserAlreadyExistsException;
import Individuals.API.mapper.KeycloakMapper;
import individuals.api.individuals.dto.TokenResponse;
import individuals.api.individuals.dto.UserLoginRequest;
import individuals.api.individuals.dto.UserRegistrationRequest;
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
    public static final String BASE_URL = "http://localhost:8080";

    private final KeycloakProperties keycloakProperties;
    private final KeycloakMapper keycloakMapper;
    private final WebClient webClient;

    public KeycloakClient(KeycloakProperties keycloakProperties, KeycloakMapper keycloakMapper) {
        this.keycloakProperties = keycloakProperties;
        this.keycloakMapper = keycloakMapper;
        this.webClient = WebClient.builder().baseUrl(BASE_URL).build();
    }

    public Mono<TokenResponse> requestToken(UserLoginRequest userLoginRequest) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("username", userLoginRequest.getEmail());
        form.add("password", userLoginRequest.getPassword());


        return webClient.post()
                .uri("/realms/my-realm/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        response -> Mono.error(new UnauthorizedException("Неверный логин или пароль")))
                .bodyToMono(TokenResponse.class);
    }


    public Mono<Void> createUser(UserRegistrationRequest userRegistrationRequest, TokenResponse accessToken) {
        var body = keycloakMapper.toCreateUserRequest(userRegistrationRequest);
        return webClient.post()
                .uri("/admin/realms/{realm}/users", keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken.getAccessToken())
                .bodyValue(body)
                .retrieve().onStatus(status -> status.value() == 409,
                        response -> Mono.error(new UserAlreadyExistsException("Пользователь с таким email уже существует")))
                .toBodilessEntity().doOnError(x -> log.error("Пользователь с email {} уже существует", userRegistrationRequest.getEmail())).then();
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("refresh_token", refreshToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> Mono.error(new UnauthorizedException("Неверный рефреш токен")))
                .bodyToMono(TokenResponse.class)
                .doOnError(x -> log.error("Неверный рефреш токен"));


    }

//    public Mono<Boolean> findUserByUsername(String username, TokenResponse token) {
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/admin/realms/{realm}/users")
//                        .queryParam("username", username)
//                        .queryParam("exact", true)
//                        .build(keycloakProperties.getRealm()))
//                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token.getAccessToken())
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<Object>>() {})
//                .map(users -> !users.isEmpty()) // Если список не пуст — юзер существует
//                .defaultIfEmpty(false)
//                .onErrorResume(e -> Mono.just(false)); // При ошибке считаем, что не нашли
//    }

}
