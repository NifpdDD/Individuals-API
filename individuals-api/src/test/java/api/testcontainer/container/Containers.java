package api.testcontainer.container;

import lombok.experimental.UtilityClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@UtilityClass
public class Containers {

    public PostgreSQLContainer postgres = PostgresTestContainer.postgresTestContainer;
    public GenericContainer keycloak = KeycloakTestContainer.keycloakTestContainer;


    public void run() {
        postgres.start();
        keycloak.start();
    }
}
