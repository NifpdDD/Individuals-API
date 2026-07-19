package personservice.testcontainer.service;

import com.example.person.dto.IndividualDto;
import com.example.person.dto.IndividualPageDto;
import com.example.person.dto.IndividualWriteDto;
import com.example.person.dto.IndividualWriteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import personservice.entity.Individual;
import personservice.exception.NotFoundException;
import personservice.repository.IndividualRepository;

import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PersonTestService {

    private final RestTemplate restTemplate;
    private final IndividualRepository individualRepository;
    private final Environment env;

    private String getBaseUrl() {
        Integer port = env.getProperty("local.server.port", Integer.class);
        if (port == null || port == 0) {
            port = env.getProperty("server.port", Integer.class, 8080);
        }
        return "http://localhost:" + port + "/v1/persons";
    }

    public IndividualWriteResponseDto createIndividual(IndividualWriteDto individualWriteDto) {
        return restTemplate.postForObject(getBaseUrl(), individualWriteDto, IndividualWriteResponseDto.class);
    }

    public IndividualWriteResponseDto updateIndividual(String id,IndividualWriteDto individualWriteDto) {
        return restTemplate.exchange(getBaseUrl()+"/{id}", HttpMethod.PUT, new HttpEntity<>(individualWriteDto), IndividualWriteResponseDto.class,id).getBody();
    }

    public void deleteIndividual(UUID individualId) {
        restTemplate.delete(getBaseUrl() + "/" + individualId);
    }

    public void compensateRegistrationIndividual(String individualId) {
        restTemplate.delete(getBaseUrl() + "/compensate-registration/" + individualId);
    }

    public IndividualDto findIndividualById(String individualId) {
        return restTemplate.getForObject(getBaseUrl() + "/" + individualId, IndividualDto.class);
    }

    public IndividualPageDto findIndividualByEmail(String email) {
        var params = new HashMap<String, String>();
        params.put("email", email);

        return restTemplate.exchange(getBaseUrl() + "?email={email}", HttpMethod.GET, HttpEntity.EMPTY, IndividualPageDto.class, params).getBody();
    }

    public void deleteAll() {
        individualRepository.deleteAll();
    }

    public Individual findRowById(String individualId) {
        return individualRepository.findById(UUID.fromString(individualId)).orElseThrow(() -> new NotFoundException(String.format("Individual with id %s not found", individualId)));
    }


}
