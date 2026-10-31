package transaction_service.rest;

import com.example.transaction.api.TransactionApi;
import com.example.transaction.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import transaction_service.entity.enums.TransactionType;
import transaction_service.dto.ValidatedDepositDto;
import transaction_service.dto.ValidatedTransferDto;
import transaction_service.dto.ValidatedWithdrawalDto;
import transaction_service.service.transaction.InitService;
import transaction_service.service.token.TokenService;
import transaction_service.service.transaction.TransactionService;

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
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(UUID transactionId) {
        return ResponseEntity.ok(transactionService.getStatusById(transactionId));
    }

    @Override
    public ResponseEntity<InitResponseDto> initTransaction(String type, Map<String, Object> request) {
        TransactionType txType = TransactionType.fromString(type);
        Object dto = objectMapper.convertValue(request, txType.getDtoClass());
        //вынести на уровень бизнес логики
        InitResponseDto responseDto = switch (txType) {
            case DEPOSIT -> initService.initDeposit((DepositInitDto) dto);
            case WITHDRAWAL -> initService.initWithdrawal((WithdrawalInitDto) dto);
            case TRANSFER -> initService.initTransfer((TransferInitDto) dto);
        };
        return ResponseEntity.ok(responseDto);
    }


}
