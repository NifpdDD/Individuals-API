package personservice.testcontainer.container;

import lombok.experimental.UtilityClass;
import org.testcontainers.containers.PostgreSQLContainer;
import personservice.testcontainer.util.Setting;

@UtilityClass
public class PostgresTestContainer {

    public PostgreSQLContainer postgresTestContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("person")
            .withUsername("admin")
            .withPassword("admin")
            .withNetwork(Setting.GLOBAL_NETWORK)
            .withNetworkAliases("postgres");
}

