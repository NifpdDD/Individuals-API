package api.mapper;

import individuals.api.individuals.dto.IndividualWriteDto;
import individuals.api.keycloak.dto.CreateUserRequest;
import individuals.api.keycloak.dto.Credential;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface KeycloakMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "username", source = "request.email")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "credentials", expression = "java(buildCredentials(request))")
    @Mapping(target = "attributes", expression = "java(buildAttributes(uuid))")
    CreateUserRequest toCreateUserRequest(IndividualWriteDto request, UUID uuid);

    default List<Credential> buildCredentials(IndividualWriteDto request) {
        Credential credential = new Credential();
        credential.setTemporary(false);
        credential.setType(Credential.TypeEnum.PASSWORD);
        credential.setValue(request.getPassword());
        return List.of(credential);
    }

    default Map<String, List<String>> buildAttributes(UUID uuid) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("uuid", List.of(uuid.toString()));
        return attributes;
    }

}
