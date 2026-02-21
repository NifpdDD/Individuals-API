package Individuals.API.service;

import Individuals.API.client.KeycloakClient;
import com.example.ApiException;
import com.example.dto.TokenResponse;
import com.example.dto.UserInfoResponse;
import com.example.dto.UserLoginRequest;
import com.example.dto.UserRegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final KeycloakClient keycloakClient;
    private final TokenService tokenService;

    public Mono<TokenResponse> register(UserRegistrationRequest userRegistrationRequest) {
        keycloakClient.createUser(userRegistrationRequest);
        return tokenService.login(new UserLoginRequest().email(userRegistrationRequest.getEmail()).password(userRegistrationRequest.getPassword()));
    }

    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(UserService::getUserInfoResponseMono)
                .switchIfEmpty(Mono.error(new ApiException("No authentication present")));
    }

    private static Mono<UserInfoResponse> getUserInfoResponseMono(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            var userInfoResponse = new UserInfoResponse();
            userInfoResponse.setId(jwt.getSubject());
            userInfoResponse.setEmail(jwt.getClaimAsString("email"));
            userInfoResponse.setRoles(jwt.getClaimAsStringList("roles"));

            if (jwt.getIssuedAt() != null) {
                userInfoResponse.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            }
            log.info("User[email={}] was successfully get info", jwt.getClaimAsString("email"));

            return Mono.just(userInfoResponse);
        }

        log.error("Can not get current user info: Invalid principal");
        return Mono.error(new ApiException("Can not get current user info: Invalid principal"));
    }


}
