package transaction_service.container.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import transaction_service.container.util.Setting;

public class PostgresTestContainer {
    public static final PostgreSQLContainer<?> postgresTestContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("transaction")
            .withUsername("admin")
            .withPassword("admin")
            .withNetwork(Setting.GLOBAL_NETWORK)
            .withNetworkAliases("postgres");
}
