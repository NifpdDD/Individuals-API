package api.mapper;

import individuals.api.individuals.dto.IndividualWriteDto;
import individuals.api.individuals.dto.IndividualWriteResponseDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import javax.validation.Valid;

@Mapper(componentModel = "spring",injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PersonMapper {

    com.example.person.dto.@Valid IndividualWriteDto from(IndividualWriteDto dto);

    IndividualWriteResponseDto from(com.example.person.dto.IndividualWriteResponseDto dto);
}
