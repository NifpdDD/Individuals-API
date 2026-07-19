package api.service;

import api.mapper.PersonMapper;
import com.example.person.api.PersonApiClient;
import individuals.api.individuals.dto.IndividualWriteDto;
import individuals.api.individuals.dto.IndividualWriteResponseDto;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PersonService {
    private final PersonMapper personMapper;
    private final PersonApiClient personApiClient;

    @WithSpan("personService.registration")
    public Mono<IndividualWriteResponseDto> createPerson(IndividualWriteDto individual) {
        return Mono.fromCallable(() -> personApiClient.registration(personMapper.from(individual)))
                .mapNotNull(HttpEntity::getBody)
                .map(personMapper::from).subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Person registered id = [{}]", t.getId()));
    }

    @WithSpan("personService.compensateRegistration")
    public Mono<Void> completeRegistration(UUID id) {
        return Mono.fromRunnable(() -> personApiClient.compensateRegistration(id))
                .subscribeOn(Schedulers.boundedElastic()).then();

    }

}
