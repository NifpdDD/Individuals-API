package personservice.service;

import com.example.person.dto.IndividualDto;
import com.example.person.dto.IndividualPageDto;
import com.example.person.dto.IndividualWriteDto;
import com.example.person.dto.IndividualWriteResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import personservice.exception.NotFoundException;
import personservice.mapper.IndividualMapper;
import personservice.repository.AddressRepository;
import personservice.repository.IndividualRepository;
import personservice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndividualService {

    private final IndividualRepository individualRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final IndividualMapper individualMapper;

    @Transactional
    public void hardDelete(UUID id) {
        var individual = individualRepository.findById(id).orElseThrow(()->new NotFoundException(String.format("Individual with id %s not found", id)));
        individualRepository.delete(individual);
        log.info("IN - hardDelete: individual with id = [{}] successfully deleted", "id");
    }

    @Transactional
    public void softDelete(UUID id) {
        var individual=individualRepository.findByIdAndActiveIsTrue(id).orElseThrow(()->new NotFoundException(String.format("Individual with id %s not found", id)));
        log.info("IN - softDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.softDelete(id);
        userRepository.softDelete(individual.getUser().getId());
        addressRepository.softDelete(individual.getUser().getAddress().getId());
    }

    @Transactional
    public IndividualWriteResponseDto register(IndividualWriteDto individualWriteDto) {
        var individual = individualMapper.to(individualWriteDto);
        individualRepository.save(individual);
        log.info("IN - create: individual with id = [{}] successfully created", individual.getId());
        return new IndividualWriteResponseDto().id(individual.getId().toString());
    }

    public IndividualDto findbyId(UUID id) {
        var individual = individualRepository.findByIdAndActiveIsTrue(id).orElseThrow(()->new NotFoundException(String.format("Individual with id %s not found", id)));
        log.info("IN - findById: individual with id = [{}] successfully found", id);
        return individualMapper.from(individual);
    }

    public IndividualPageDto findbyEmails (List<String> emails, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var individuals = individualRepository.findAllByEmails(emails,pageable);
        if (individuals.isEmpty()) {
            throw new NotFoundException(String.format("Individual with emails %s not found", emails));
        }
        log.info("IN - findbyEmails: individual with emails = [{}]", emails);
        var from = individualMapper.from(individuals.getContent());
        return new IndividualPageDto().items(from);
    }

    @Transactional
    public IndividualWriteResponseDto update(UUID id, IndividualWriteDto individualWriteDto) {
        var individual = individualRepository.findById(id).orElseThrow(()->new NotFoundException(String.format("Individual with id %s not found", id)));
        individualMapper.update( individual, individualWriteDto);
        individualRepository.save(individual);
        log.info("IN - update: individual with id = [{}]", individual.getId());
        return new IndividualWriteResponseDto().id(individual.getId().toString());
    }


}
