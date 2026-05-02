package Individuals.API.service;

import Individuals.API.client.KeycloakClient;
import Individuals.API.client.KeycloakProperties;
import Individuals.API.exception.keycloak.UnauthorizedException;
import Individuals.API.exception.keycloak.UserNotFoundException;
import individuals.api.individuals.dto.TokenResponse;
import individuals.api.individuals.dto.UserInfoResponse;
import individuals.api.individuals.dto.UserLoginRequest;
import individuals.api.individuals.dto.UserRegistrationRequest;
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
    private final AdminTokenProvider adminTokenProvider;

    public Mono<TokenResponse> register(UserRegistrationRequest userRegistrationRequest)  {
       return adminTokenProvider.getAdminToken()
                .flatMap(token ->
                        keycloakClient.createUser(userRegistrationRequest, token)
                                .then(tokenService.login(new UserLoginRequest()
                                        .email(userRegistrationRequest.getEmail())
                                        .password(userRegistrationRequest.getPassword())))
                ).doOnNext(x-> log.info("User[email={}] was successfully registered", userRegistrationRequest.getEmail()));
    }


    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(UserService::getUserInfoResponseMono)
                .switchIfEmpty(Mono.error(new UnauthorizedException("No authentication present")))
                .doOnError(x-> log.error("Can not get current user info", x));
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

        return Mono.error(new UnauthorizedException("Can not get current user info: Invalid principal"));
    }


}
