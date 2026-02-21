package Individuals.API.service;

import Individuals.API.client.KeycloakClient;
import com.example.dto.TokenRefreshRequest;
import com.example.dto.TokenResponse;
import com.example.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class TokenService {
    private final KeycloakClient keycloakClient;

    public Mono<TokenResponse> login(UserLoginRequest loginRequest) {
        return keycloakClient.requestToken(loginRequest);
    }

    public Mono<TokenResponse> refresh(TokenRefreshRequest refreshRequest) {
        return keycloakClient.refreshToken(refreshRequest.getRefreshToken());
    }
}
