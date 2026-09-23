package transaction_service.rest;

import com.example.transaction.api.TransactionApi;
import com.example.transaction.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import transaction_service.entity.enums.TransactionType;
import transaction_service.exceptions.NotSupportedTypeException;
import transaction_service.model.ValidatedDepositDto;
import transaction_service.model.ValidatedTransferDto;
import transaction_service.model.ValidatedWithdrawalDto;
import transaction_service.service.InitService;
import transaction_service.service.TokenService;
import transaction_service.service.TransactionService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionRestContoller implements TransactionApi {

    private final ObjectMapper objectMapper;
    private final InitService initService;
    private final TokenService tokenService;
    private final TransactionService transactionService;

    @Override
    public ResponseEntity<TransactionConfirmResponse> confirmOperation(UUID transactionId, TransactionConfirmRequestDto transactionConfirmRequestDto) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId", transactionId.toString())) {
            var transaction = tokenService.validateToken(transactionConfirmRequestDto.getToken(), transactionId);
            TransactionConfirmResponse response = switch (transaction.getType()) {
                case DEPOSIT -> transactionService.confirmDeposit(transactionId, (ValidatedDepositDto) transaction);
                case WITHDRAWAL ->
                        transactionService.confirmWithdrawal(transactionId, (ValidatedWithdrawalDto) transaction);
                case TRANSFER -> transactionService.confirmTransfer(transactionId, (ValidatedTransferDto) transaction);
            };
            return ResponseEntity.ok(response);
        }
    }

    @Override
    public ResponseEntity<InitResponseDto> initTransaction(String type, Map<String, Object> request) {
        TransactionType txType = TransactionType.fromString(type);
        Object dto = objectMapper.convertValue(request, txType.getDtoClass());
        UUID transactionId = UUID.randomUUID();
        InitResponseDto responseDto = switch (txType) {
            case DEPOSIT -> initService.initDeposit((DepositInitDto) dto, transactionId);
            case WITHDRAWAL -> initService.initWithdrawal((WithdrawalInitDto) dto, transactionId);
            case TRANSFER -> initService.initTransfer((TransferInitDto) dto, transactionId);
        };
        return ResponseEntity.ok(responseDto);
    }
}
