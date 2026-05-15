package api.testcontainer.data;

import individuals.api.individuals.dto.UserLoginRequest;
import individuals.api.individuals.dto.UserRegistrationRequest;
import org.springframework.stereotype.Component;


@Component
public class DtoCreator {
    public UserRegistrationRequest buildIndividualWriteDto() {

        var request = new UserRegistrationRequest();
        request.setEmail("test@mail.com");
        request.setPassword("secret123");
        request.setConfirmPassword("secret123");

        return request;
    }

    public UserLoginRequest buildUserLoginRequest() {
        var request = new UserLoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("secret123");
        return request;
    }


}
