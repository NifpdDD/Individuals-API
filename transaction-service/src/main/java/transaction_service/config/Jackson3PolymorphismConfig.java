import com.example.transaction.dto.DepositInitDto;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransferInitDto;
import com.example.transaction.dto.WithdrawalInitDto;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Jackson3PolymorphismConfig {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type", // Jackson будет искать это поле в JSON-теле запроса
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DepositInitDto.class, name = "deposit"),
            @JsonSubTypes.Type(value = WithdrawalInitDto.class, name = "withdrawal"),
            @JsonSubTypes.Type(value = TransferInitDto.class, name = "transfer")
    })
    private interface InitTransactionRequestMixIn {}

    // 2. Регистрируем этот шаблон в строителе ObjectMapper Спринга
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer configureJackson3Polymorphism() {
        return builder -> {
            // Насильно связываем аннотации шаблона со сгенерированным пустым интерфейсом
            builder.mixIn(InitTransactionRequest.class, InitTransactionRequestMixIn.class);
        };
    }
}