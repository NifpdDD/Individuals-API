package api.spec.integration;

import api.IndividualsApiApplication;
import api.testcontainer.config.AppTestConfig;
import api.testcontainer.container.Containers;
import api.testcontainer.container.KeycloakTestContainer;
import api.testcontainer.data.DtoCreator;
import api.testcontainer.service.IndividualApiTestService;
import api.testcontainer.service.KeycloakApiTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class LifecycleSpecification {
    protected final DtoCreator dtoCreator =
            new DtoCreator();

    @Autowired
    protected IndividualApiTestService individualControllerService;
    @Autowired
    protected KeycloakApiTestService keycloakApiTestService;

    static {
        Containers.run();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        final String kcBase = "http://" +
                KeycloakTestContainer.keycloakTestContainer.getHost() + ":" +
                KeycloakTestContainer.keycloakTestContainer.getFirstMappedPort();

        r.add("keycloak.server-url", () -> kcBase);
        r.add("keycloak.realm", () -> "my-realm");
        r.add("keycloak.clientId", () -> "individuals-api");
        r.add("keycloak.adminClientId", () -> "admin-cli");
        r.add("keycloak.adminUsername", () -> "admin");
        r.add("keycloak.adminPassword", () -> "admin");

        r.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> kcBase + "/realms/my-realm/protocol/openid-connect/certs");
    }




    @AfterEach
    public void clear() {
        keycloakApiTestService.clear();
    }
}
