package Individuals.API.controller;

import Individuals.API.service.TokenService;
import Individuals.API.service.UserService;

import com.example.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody Mono<UserLoginRequest> body) {
        return body.flatMap(tokenService::login).map(ResponseEntity::ok);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<TokenResponse>> register(@RequestBody Mono<UserRegistrationRequest> body) {
        return body.flatMap(userService::register).map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refresh(@RequestBody Mono<TokenRefreshRequest> body) {
        return body.flatMap(tokenService::refresh).map(ResponseEntity::ok);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> me() {
        return userService.getUserInfo().map(ResponseEntity::ok);
    }

}
