package transaction_service.entity.enums;

import com.example.transaction.dto.DepositInitDto;
import com.example.transaction.dto.TransferInitDto;
import com.example.transaction.dto.WithdrawalInitDto;
import lombok.Getter;
import transaction_service.exceptions.NotSupportedTypeException;

@Getter
public enum TransactionType {
    DEPOSIT(DepositInitDto.class),
    WITHDRAWAL(WithdrawalInitDto.class),
    TRANSFER(TransferInitDto.class);

    private final Class<?> dtoClass;

    TransactionType(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    public static TransactionType fromString(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotSupportedTypeException(String.format("Тип операции [%s] не поддерживается", type));
        }
    }

}