package personservice.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import personservice.testcontainer.container.Containers;
import personservice.testcontainer.container.PostgresTestContainer;
import personservice.testcontainer.data.DtoCreator;
import personservice.testcontainer.service.PersonTestService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class LifecycleSpecification {

    protected final DtoCreator dtoCreator = new DtoCreator();

    @Autowired
    protected PersonTestService personTestService;

    static {
        Containers.run();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", () -> PostgresTestContainer.postgresTestContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PostgresTestContainer.postgresTestContainer.getUsername());
        registry.add("spring.datasource.password", () -> PostgresTestContainer.postgresTestContainer.getPassword());

    }

    @AfterEach
    protected void cleanUp() {
        personTestService.deleteAll();
    }



}
