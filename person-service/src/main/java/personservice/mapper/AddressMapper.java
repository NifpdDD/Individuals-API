package personservice.mapper;

import com.example.person.dto.AddressDto;
import com.example.person.dto.IndividualWriteDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import personservice.entity.Address;
import personservice.entity.Country;
import personservice.entity.User;
import personservice.exception.BaseExeption;
import personservice.repository.CountryRepository;
import personservice.util.DateTimeUtil;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
@Setter(onMethod_ = @Autowired)
public abstract class AddressMapper {

    protected DateTimeUtil dateTimeUtil;
    protected CountryRepository countryRepository;

    @Named("toAddress")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "address", source = "address.address")
    @Mapping(target = "country", source = "address.countryCode", qualifiedByName = "toCountry")
    public abstract Address to(IndividualWriteDto dto);

    @Named("fromAddress")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "countryCode", source = "country.code")
    public abstract AddressDto from(Address address);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "address", source = "address.address")
    @Mapping(target = "country", source = "address.countryCode", qualifiedByName = "toCountry")
    public abstract Address update(
            @MappingTarget
            Address address,
            IndividualWriteDto dto
    );

    @Named("toCountry")
    public Country toCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new BaseExeption("Unknow country code: [%s]", countryCode));
    }

    public Address update(User user, IndividualWriteDto dto) {
        return update(user.getAddress(), dto);
    }
}
