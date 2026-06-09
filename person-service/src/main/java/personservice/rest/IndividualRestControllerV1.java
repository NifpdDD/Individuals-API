package personservice.rest;

import com.example.person.api.PersonApi;
import com.example.person.dto.IndividualDto;
import com.example.person.dto.IndividualPageDto;
import com.example.person.dto.IndividualWriteDto;
import com.example.person.dto.IndividualWriteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import personservice.service.IndividualService;

import javax.validation.constraints.Email;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class IndividualRestControllerV1 implements PersonApi {

    private final IndividualService individualService;


    @Override
    public ResponseEntity<Void> compensateRegistration(UUID id) {
        individualService.hardDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        individualService.softDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<IndividualPageDto> findAllByEmail(List<@Email String> email, Integer page, Integer size) {
        var individuals = individualService.findbyEmails(email,page,size);
        return ResponseEntity.ok(individuals);
    }

    @Override
    public ResponseEntity<IndividualDto> findById(UUID id) {
        var individual = individualService.findbyId(id);
        return ResponseEntity.ok(individual);
    }

    @Override
    public ResponseEntity<IndividualWriteResponseDto> registration(IndividualWriteDto individualWriteDto) {
        var individual = individualService.register(individualWriteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(individual);
    }

    @Override
    public ResponseEntity<IndividualWriteResponseDto> update(UUID id, IndividualWriteDto individualWriteDto) {
        var individual = individualService.update(id, individualWriteDto);
        return ResponseEntity.ok(individual);
    }
}
