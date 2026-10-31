package transaction_service.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import transaction_service.container.config.AppTestConfig;
import transaction_service.container.containers.TestContainersConfig;
import transaction_service.container.containers.data.DtoCreator;
import transaction_service.container.service.KafkaExternalProducerService;
import transaction_service.container.service.TransactionTestService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AppTestConfig.class)
@ActiveProfiles("test")
public abstract class LifecycleSpecification {

    static {
        TestContainersConfig.run();
    }

    @DynamicPropertySource
    static void configureContainersProperties(DynamicPropertyRegistry registry) {
        TestContainersConfig.linkProperties(registry);
    }

    @Autowired
    protected TransactionTestService transactionTestService;

    @Autowired
    protected KafkaExternalProducerService kafkaExternalProducerService;

    protected final DtoCreator dtoCreator = new DtoCreator();


    @AfterEach
    protected void cleanUp() {
        transactionTestService.deleteAll();
    }

}
