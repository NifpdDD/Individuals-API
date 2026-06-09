package api.testcontainer.data;

import individuals.api.individuals.dto.AddressWriteDto;
import individuals.api.individuals.dto.IndividualWriteDto;
import individuals.api.individuals.dto.UserLoginRequest;
import org.springframework.stereotype.Component;

@Component
public class DtoCreator {

    public IndividualWriteDto buildIndividualWriteDto() {
        var addressDto = new AddressWriteDto()
                .address("123 Main Street")
                .city("New York")
                .zipCode("10001")
                .countryCode("US");

        return new IndividualWriteDto()
                .firstName("John")
                .lastName("Doe")
                .email("test@mail.com")
                .password("secret123")
                .confirmPassword("secret123")
                .passportNumber("1234567890")
                .phoneNumber("+79991112233")
                .address(addressDto);
    }

    public UserLoginRequest buildUserLoginRequest() {
        return new UserLoginRequest()
                .email("test@mail.com")
                .password("secret123");
    }
}
