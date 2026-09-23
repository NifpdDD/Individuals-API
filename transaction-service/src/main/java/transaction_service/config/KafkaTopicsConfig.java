package transaction_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    public static final String TOPIC_DEPOSIT_IN = "deposit_transactions_topic_in";
    public static final String TOPIC_WITHDRAWAL_IN = "withdrawal_transactions_topic_in";
    public static final String TOPIC_DEPOSIT_OUT = "all_transactions_topic_out";;

    @Bean
    public NewTopic depositEventsTopicIn() {
        return TopicBuilder.name(TOPIC_DEPOSIT_IN)
                .partitions(3)          // 3 партиции позволят параллельно читать топик трем инстансам другого сервиса
                .replicas(1)            // Для локальной разработки (в продакшене ставьте минимум 3)
                .config("min.insync.replicas", "1") // Согласуется со значением acks=all
                .build();
    }

    @Bean
    public NewTopic withdrawalEventsTopicIn() {
        return TopicBuilder.name(TOPIC_WITHDRAWAL_IN)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic allEventsTopicOut() {
        return TopicBuilder.name(TOPIC_DEPOSIT_OUT)
                .partitions(3)          // 3 партиции позволят параллельно читать топик трем инстансам другого сервиса
                .replicas(1)            // Для локальной разработки (в продакшене ставьте минимум 3)
                .config("min.insync.replicas", "1") // Согласуется со значением acks=all
                .build();
    }

}