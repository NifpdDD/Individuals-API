package transaction_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import transaction_service.dto.ExternalTransactionResponseDto;
import transaction_service.dto.TransactionEventDto;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.topics.transactions-in}")
    private String transactionsInTopic;

    @Value("${spring.kafka.topics.transactions-out}")
    private String transactionsOutTopic;

    @Value("${spring.kafka.topics.transactions-out-dlq}")
    private String transactionsOutTopicDlq;


    @Bean
    public ConsumerFactory<String, ExternalTransactionResponseDto> consumerFactory(KafkaProperties kafkaProperties) {
        JacksonJsonDeserializer<ExternalTransactionResponseDto> jacksonDeserializer =
                new JacksonJsonDeserializer<>(ExternalTransactionResponseDto.class);
        ErrorHandlingDeserializer<String> errorHandlingKeyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());
        ErrorHandlingDeserializer<ExternalTransactionResponseDto> errorHandlingValueDeserializer =
                new ErrorHandlingDeserializer<>(jacksonDeserializer);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                errorHandlingKeyDeserializer,
                errorHandlingValueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExternalTransactionResponseDto> kafkaListenerContainerFactory(
            ConsumerFactory<String, ExternalTransactionResponseDto> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ExternalTransactionResponseDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    log.error("All retry attempts exhausted! Forwarding record [Key: {}, Partition: {}, Offset: {}] to DLQ topic: {}",
                            record.key(), record.partition(), record.offset(), transactionsOutTopicDlq);
                    return new org.apache.kafka.common.TopicPartition(transactionsOutTopicDlq, -1);
                }
        );
        FixedBackOff fixedBackOff = new FixedBackOff(2000L, 2);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Retry attempt #{} failed for record [Key: {}, Offset: {}]. Technical error: ",
                    deliveryAttempt, record.key(), record.offset(), ex);
        });

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    KafkaTemplate<String, String> transactionKafkaTemplate(
            ProducerFactory<String, String> producerFactory) {
        var template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(transactionsOutTopicDlq).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic transactionTopicIn() {
        return TopicBuilder.name(transactionsInTopic).partitions(3).replicas(1).config("min.insync.replicas", "1").build();
    }

    @Bean
    public NewTopic transactionTopicOut() {
        return TopicBuilder.name(transactionsOutTopic).partitions(3).replicas(1).config("min.insync.replicas", "1").build();
    }
}
