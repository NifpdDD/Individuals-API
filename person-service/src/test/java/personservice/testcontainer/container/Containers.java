package personservice.testcontainer.container;

import lombok.experimental.UtilityClass;
import org.testcontainers.containers.PostgreSQLContainer;

@UtilityClass
public class Containers {

    public PostgreSQLContainer postgres = PostgresTestContainer.postgresTestContainer;

    public void run() {
        postgres.start();
    }
}
