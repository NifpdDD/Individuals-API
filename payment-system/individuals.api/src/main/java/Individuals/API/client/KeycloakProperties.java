package Individuals.API.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "keycloak")
@Data
public class KeycloakProperties {
    @NotBlank
    private String clientId;

    /**
     * Хост Keycloak (включая протокол)
     */
    @NotBlank
    private String baseUrl;

    /**
     * Realm в Keycloak
     */
    @NotBlank
    private String realm;

    /**
     * Endpoint для получения токена (относительный путь)
     */
    @NotBlank
    private String tokenEndpointUrl;


    @NotBlank
    private String adminEmail;

    @NotBlank
    private String adminPassword;

}
