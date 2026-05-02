package Individuals.API.service;

import Individuals.API.client.KeycloakClient;
import Individuals.API.client.KeycloakProperties;
import individuals.api.individuals.dto.TokenRefreshRequest;
import individuals.api.individuals.dto.TokenResponse;
import individuals.api.individuals.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class TokenService {
    private final KeycloakClient keycloakClient;
    private final KeycloakProperties keycloakProperties;

    private boolean isAdmin(String email) {
        return keycloakProperties.getAdminEmail().equals(email);
    }

    public Mono<TokenResponse> login(UserLoginRequest loginRequest) {
        return keycloakClient.requestToken(loginRequest)
                .doOnNext(t -> {
                    if (!isAdmin(loginRequest.getEmail())) {
                        log.info("Token successfully generated for email = [{}]", loginRequest.getEmail());
                    }
                })
                .doOnError(e -> {
                    if (!isAdmin(loginRequest.getEmail())) {
                        log.error("Failed to generate token for email = [{}]", loginRequest.getEmail(), e);
                    }
                });
    }

    public Mono<TokenResponse> refresh(TokenRefreshRequest refreshRequest) {
        return keycloakClient.refreshToken(refreshRequest.getRefreshToken());
    }
}
