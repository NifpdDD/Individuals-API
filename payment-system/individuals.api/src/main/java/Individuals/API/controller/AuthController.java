package Individuals.API.controller;

import Individuals.API.exception.keycloak.ValidationEcxeption;
import Individuals.API.service.TokenService;
import Individuals.API.service.UserService;

import individuals.api.individuals.api.AuthApi;
import individuals.api.individuals.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api/auth/v1")
@RequiredArgsConstructor
@Validated
public class AuthController  {

    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody Mono<UserLoginRequest> body) {
        return body.flatMap(tokenService::login).map(ResponseEntity::ok);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<TokenResponse>> register(@Valid @RequestBody Mono<UserRegistrationRequest> body) {
        return body.flatMap(requestBody->{if (!requestBody.getPassword().equals(requestBody.getConfirmPassword())) {
            return Mono.error(new ValidationEcxeption("Пароли не совпадают"));
        }
        return userService.register(requestBody);
        }).map(x->ResponseEntity.status(HttpStatus.CREATED).body(x));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refresh(@Valid @RequestBody Mono<TokenRefreshRequest> body) {
        return body.flatMap(tokenService::refresh).map(ResponseEntity::ok);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> me() {
        return userService.getUserInfo().map(ResponseEntity::ok);
    }

}
