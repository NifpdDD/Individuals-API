package transaction_service.container.containers;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.DynamicPropertyRegistry;

@UtilityClass
public class TestContainersConfig {


   public void run() {
       PostgresTestContainer.postgresTestContainer.start();
       KafkaTestContainer.kafkaTestContainer.start();
    }

    public void linkProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestContainer.postgresTestContainer::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresTestContainer.postgresTestContainer::getUsername);
        registry.add("spring.datasource.password", PostgresTestContainer.postgresTestContainer::getPassword);
        registry.add("spring.kafka.bootstrap-servers",  KafkaTestContainer.kafkaTestContainer::getBootstrapServers);
    }
}
