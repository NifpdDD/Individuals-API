package transaction_service.mapper;

import com.example.transaction.dto.WalletDto;
import com.example.transaction.dto.WalletWriteDto;
import com.example.transaction.dto.WalletWriteResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import transaction_service.entity.Wallet;
import transaction_service.entity.enums.WalletStatus;
import transaction_service.util.DateTimeUtil;

@Mapper(
        componentModel = "spring",
        imports = { WalletStatus.class, java.math.BigDecimal.class, java.util.UUID.class }
)
public abstract class WalletMapper {

    @Autowired
    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "modifiedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "status", expression = "java(WalletStatus.ACTIVE)")
    @Mapping(target = "balance", expression = "java(BigDecimal.ZERO)")
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "uuid", expression = "java(UUID.randomUUID())")
    public abstract Wallet to(WalletWriteDto dto);

    public abstract WalletDto from(Wallet wallet);
}