package transaction_service.container.containers;

import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestContainer {
    public static final KafkaContainer kafkaTestContainer = new KafkaContainer(
            DockerImageName.parse("apache/kafka:4.1.1")
    );

}
