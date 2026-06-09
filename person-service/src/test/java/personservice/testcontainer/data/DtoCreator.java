package personservice.testcontainer.data;

import com.example.person.dto.AddressWriteDto;
import com.example.person.dto.IndividualWriteDto;
import org.springframework.stereotype.Component;

@Component
public class DtoCreator {
    public IndividualWriteDto buildNewIndividualWriteDto() {
        var request = new IndividualWriteDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassportNumber("123456789");
        request.setPhoneNumber("123456789");
        request.setAddress(buildAddressDto());
        return request;
    }

    public IndividualWriteDto buildChangedIndividualWriteDto(IndividualWriteDto oldDto) {
        oldDto.setFirstName("Pivo");
        oldDto.setLastName("Vodka");
        return oldDto;

    }

    private AddressWriteDto buildAddressDto() {
        var request = new AddressWriteDto();
        request.setAddress("123 Main St");
        request.setZipCode("12345");
        request.setCity("Oslo");
        request.setCountryCode("NOR");
        return request;
    }
}
