package transaction_service.service.transaction;

import com.example.transaction.dto.DepositInitDto;
import com.example.transaction.dto.InitResponseDto;
import com.example.transaction.dto.TransferInitDto;
import com.example.transaction.dto.WithdrawalInitDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import transaction_service.entity.Wallet;
import transaction_service.entity.enums.WalletStatus;
import transaction_service.exceptions.NotEnoughMoneyException;
import transaction_service.exceptions.WalletNotFoundOrBlockedException;
import transaction_service.repository.WalletRepository;
import transaction_service.service.commission.CommissionService;
import transaction_service.service.token.TokenService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
//Нужно ли делать интерфейсы?
public class InitService {

    private final WalletRepository walletRepository;
    private final CommissionService commissionService;
    private final TokenService tokenService;

    public InitResponseDto initDeposit(DepositInitDto depositInitDto) {
        UUID transactionId = generateID();
        validateAndGetWallet(depositInitDto.getWalletUuid(), depositInitDto.getUserUuid(), depositInitDto.getCurrency());
        var commission = commissionService.calculateCommission(depositInitDto.getAmount());
        var token = tokenService.generateTransactionTokenForDeposit(depositInitDto, commission, transactionId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId",  transactionId.toString())) {
            log.info("Deposit init for wallet with id = [{}] success", depositInitDto.getWalletUuid());
        }
        return new InitResponseDto(transactionId, token, commission);
    }


    public InitResponseDto initWithdrawal(WithdrawalInitDto withdrawalInitDto) {
        UUID transactionId = generateID();
        var wallet = validateAndGetWallet(withdrawalInitDto.getWalletUuid(), withdrawalInitDto.getUserUuid(), withdrawalInitDto.getCurrency());
        if (wallet.getBalance().compareTo(withdrawalInitDto.getAmount())<0){
            throw new NotEnoughMoneyException(String.format("Wallet with id =[%s] has not enough money",wallet.getUuid()));
        }
        var commission = commissionService.calculateCommission(withdrawalInitDto.getAmount());
        var token = tokenService.generateTransactionTokenForWithdrawal(withdrawalInitDto,commission,transactionId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId",  transactionId.toString())) {
            log.info("Withdrawal init for wallet with id = [{}] success", withdrawalInitDto.getWalletUuid());
        }
        return new InitResponseDto(transactionId, token, commission);
    }

    public InitResponseDto initTransfer(TransferInitDto transferInitDto) {
        UUID transactionId = generateID();
       var walletFrom = validateAndGetWallet(transferInitDto.getFromWalletUuid(), transferInitDto.getFromUserUuid(), transferInitDto.getCurrency());
        if (walletFrom.getBalance().compareTo(transferInitDto.getAmount())<0){
            throw new NotEnoughMoneyException(String.format("Wallet with id =[%s] has not enough money",walletFrom.getUuid()));
        }
        validateAndGetWallet(transferInitDto.getToWalletUuid(), transferInitDto.getToUserUuid(), transferInitDto.getCurrency());
        var token = tokenService.generateTransactionTokenForTransfer(transferInitDto,transactionId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("transactionId",  transactionId.toString())) {
            log.info("Transfer init from wallet with id = [{}] to wallet with id = [{}]success", transferInitDto.getFromWalletUuid(), transferInitDto.getToWalletUuid());
        }
        return new InitResponseDto(transactionId,token, BigDecimal.ZERO);
    }

    private Wallet validateAndGetWallet(UUID walletUuid, UUID userUuid, String currency) {
        Wallet wallet = walletRepository.findWalletByUserIdAndWalletTypeCurrencyCodeAndStatus(
                walletUuid, userUuid, currency, WalletStatus.ACTIVE
        ).orElseThrow(() -> new WalletNotFoundOrBlockedException(
                String.format("Wallet with id = [%s] not found or blocked", walletUuid)
        ));
        log.debug("Wallet with id = [{}] found", walletUuid);
        return wallet;
    }

    private  UUID generateID() {
        return UUID.randomUUID();
    }
}
