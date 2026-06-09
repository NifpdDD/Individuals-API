package api.mapper;

import individuals.api.individuals.dto.IndividualWriteDto;
import individuals.api.keycloak.dto.CreateUserRequest;
import individuals.api.keycloak.dto.Credential;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface KeycloakMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "username", source = "email")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "email")
    @Mapping(target = "lastName", source = "email")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "credentials", expression = "java(buildCredentials(request))")
    CreateUserRequest toCreateUserRequest(IndividualWriteDto request);

    default List<Credential> buildCredentials(IndividualWriteDto request) {
        Credential credential = new Credential();
        credential.setTemporary(false);
        credential.setType(Credential.TypeEnum.PASSWORD);
        credential.setValue(request.getPassword());
        return List.of(credential);
    }

}
