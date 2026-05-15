package api.testcontainer.service;

import individuals.api.individuals.dto.TokenResponse;
import individuals.api.individuals.dto.UserInfoResponse;
import individuals.api.individuals.dto.UserLoginRequest;
import individuals.api.individuals.dto.UserRegistrationRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IndividualApiTestService {

    private final RestTemplate restTemplate;
    private final Environment env;

    public IndividualApiTestService(RestTemplate restTemplate, Environment env) {
        this.restTemplate = restTemplate;
        this.env = env;
    }

    private String baseUrl() {
        Integer port = env.getProperty("local.server.port", Integer.class);
        if (port == null || port == 0) {
            port = env.getProperty("server.port", Integer.class, 8080);
        }
        return "http://localhost:" + port + "/api/v1/auth";
    }

    public TokenResponse register(UserRegistrationRequest request) {
        return restTemplate.postForObject(baseUrl() + "/register", request, TokenResponse.class);
    }

    public TokenResponse login(UserLoginRequest request) {
        return restTemplate.postForObject(baseUrl() + "/login", request, TokenResponse.class);
    }

    public UserInfoResponse getMe(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var resp = restTemplate.exchange(baseUrl() + "/me", HttpMethod.GET, new HttpEntity<>(headers), UserInfoResponse.class);
        return resp.getBody();
    }
}
