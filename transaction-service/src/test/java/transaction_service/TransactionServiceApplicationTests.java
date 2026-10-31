package transaction_service;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import transaction_service.container.config.AppTestConfig;
import transaction_service.integration.LifecycleSpecification;

@Import(AppTestConfig.class)
class TransactionServiceApplicationTests extends LifecycleSpecification {

	@Test
	void contextLoads() {
	}

}
