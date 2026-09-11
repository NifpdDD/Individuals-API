package transaction_service.rest;

import com.example.transaction.api.TransactionApi;
import com.example.transaction.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import transaction_service.exceptions.NotSupportedType;
import transaction_service.service.InitService;

@RestController
@RequiredArgsConstructor
public class TransactionRestContoller implements TransactionApi {
    private final InitService initService;
    @Override
    public ResponseEntity<TransactionConfirmResponse> confirmOperation(String type, InitResponseDto initResponseDto) {
        return null;
    }

    @Override
    public ResponseEntity<InitResponseDto> initTransaction(String type, InitTransactionRequest initTransactionRequest) {
        String normalizedType = type != null ? type.toLowerCase() : "";

        InitResponseDto responseDto = switch (initTransactionRequest) {
            case DepositInitDto deposit when normalizedType.equals("deposit") -> initService.initDeposit(deposit);
            case WithdrawalInitDto withdrawal when normalizedType.equals("withdrawal") -> initService.initWithdrawal(withdrawal);
            case TransferInitDto transfer when normalizedType.equals("transfer") -> initService.initTransfer(transfer);
            default -> throw new NotSupportedType(
                    String.format("Сочетание типа [%s] и класса запроса [%s] не поддерживается",
                            type, initTransactionRequest.getClass().getSimpleName())
            );
        };

        return ResponseEntity.ok(responseDto);
    }
}
