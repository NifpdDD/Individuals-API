package api.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
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

    @NotBlank
    private String adminClientId;

    @NotBlank
    private String serverUrl;

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
    private String realmUrl;


    @NotBlank
    private String adminEmail;

    @NotBlank
    private String adminPassword;

}
