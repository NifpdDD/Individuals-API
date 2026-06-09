package api;

import com.example.person.api.PersonApiClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = {PersonApiClient.class})
public class IndividualsApiApplication {

	public static void main(String[] args) {
 		SpringApplication.run(IndividualsApiApplication.class, args);
	}

}
