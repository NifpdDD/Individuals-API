package Individuals.API.service;

import Individuals.API.client.KeycloakProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import individuals.api.individuals.dto.TokenResponse;
import individuals.api.individuals.dto.UserLoginRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminTokenProvider {

    public static final String ADMIN_TOKEN = "admin_token";
    private final KeycloakProperties keycloakProperties;
    private final TokenService tokenService;

    private Cache<String, TokenResponse> adminTokenCache;

    @PostConstruct
    public void initCache() {
        this.adminTokenCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(1)
                .build();
    }

    public Mono<TokenResponse> getAdminToken() {
        String cacheKey = ADMIN_TOKEN;

        TokenResponse cached = adminTokenCache.getIfPresent(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        UserLoginRequest admin = new UserLoginRequest()
                .email(keycloakProperties.getAdminEmail())
                .password(keycloakProperties.getAdminPassword());

        return tokenService.login(admin)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .doOnNext(token -> adminTokenCache.put(cacheKey, token))
                .doOnError(error -> {
                    log.error("Ошибка получения админ-токена, инвалидируем кэш", error);
                    adminTokenCache.invalidate(cacheKey);
                });
    }
}