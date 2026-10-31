package transaction_service.container.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class AppTestConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public KafkaTemplate<String, Object> transactionExternalEmulatorKafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        var template = new KafkaTemplate<>(producerFactory);
        return template;
    }
}
