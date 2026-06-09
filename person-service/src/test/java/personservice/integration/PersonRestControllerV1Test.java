package personservice.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import personservice.exception.BaseExeption;

public class PersonRestControllerV1Test extends LifecycleSpecification{



    @Test
    public void shouldReturnPerson() {
        var request = dtoCreator.buildNewIndividualWriteDto();
        var individual = personTestService.createIndividual(request);
        var answer = personTestService.findIndividualById(individual.getId());
        Assertions.assertEquals(request.getFirstName(), answer.getFirstName());
        Assertions.assertEquals(request.getLastName(), answer.getLastName());
        Assertions.assertEquals(request.getEmail(), answer.getEmail());
        Assertions.assertEquals(request.getPhoneNumber(), answer.getPhoneNumber());
        Assertions.assertEquals(request.getAddress().getCity(), answer.getAddress().getCity());
        Assertions.assertEquals(request.getAddress().getAddress(), answer.getAddress().getAddress());
        Assertions.assertEquals(request.getAddress().getZipCode(), answer.getAddress().getZipCode());
        Assertions.assertEquals(request.getAddress().getCountryCode(), answer.getAddress().getCountryCode());
    }

    @Test
    public void shouldUpdatePerson() {
        var request = dtoCreator.buildNewIndividualWriteDto();
        var answerCreate =personTestService.createIndividual(request);
        var requstUpdate = dtoCreator.buildChangedIndividualWriteDto(request);
        var newIndividual = personTestService.updateIndividual(answerCreate.getId(),requstUpdate);
        var answerUpdate = personTestService.findIndividualById(newIndividual.getId());
        Assertions.assertEquals(requstUpdate.getFirstName(), answerUpdate.getFirstName());
        Assertions.assertEquals(requstUpdate.getLastName(), answerUpdate.getLastName());
        Assertions.assertEquals(requstUpdate.getEmail(), answerUpdate.getEmail());
        Assertions.assertEquals(requstUpdate.getPhoneNumber(), answerUpdate.getPhoneNumber());
        Assertions.assertEquals(requstUpdate.getAddress().getCity(), answerUpdate.getAddress().getCity());
        Assertions.assertEquals(requstUpdate.getAddress().getAddress(), answerUpdate.getAddress().getAddress());
        Assertions.assertEquals(requstUpdate.getAddress().getZipCode(), answerUpdate.getAddress().getZipCode());
        Assertions.assertEquals(requstUpdate.getAddress().getCountryCode(), answerUpdate.getAddress().getCountryCode());
    }

    @Test
    public void shouldDeletePerson() {
        var request = dtoCreator.buildNewIndividualWriteDto();
        var answerCreate =personTestService.createIndividual(request);
        personTestService.compensateRegistrationIndividual(answerCreate.getId());
        Assertions.assertThrows(BaseExeption.class, () -> personTestService.findRowById(answerCreate.getId()));
    }




}
