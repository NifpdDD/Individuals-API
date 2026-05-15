package api.testcontainer.container;

import api.testcontainer.util.Setting;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

public class PostgresTestContainer {

    public static final PostgreSQLContainer postgresTestContainer = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("keycloak")
                .withUsername("keycloak")
                .withPassword("keycloak")
                .withNetwork(Setting.GLOBAL_NETWORK)
                .withNetworkAliases("postgres");
}
