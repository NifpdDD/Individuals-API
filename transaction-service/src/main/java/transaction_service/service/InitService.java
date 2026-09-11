package transaction_service.service;

import com.example.transaction.dto.DepositInitDto;
import com.example.transaction.dto.InitResponseDto;
import com.example.transaction.dto.TransferInitDto;
import com.example.transaction.dto.WithdrawalInitDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transaction_service.entity.enums.WalletStatus;
import transaction_service.exceptions.WalletNotFoundOrBlockedException;
import transaction_service.repository.WalletRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitService {

    private final WalletRepository walletRepository;
    private final CommissionService commissionService;
    private final TokenService tokenService;

    public InitResponseDto initDeposit(DepositInitDto depositInitDto) {
        walletRepository.findWalletByUserIdAndWalletTypeCurrencyCodeAndStatus(depositInitDto.getWalletUuid(),
                depositInitDto.getUserUuid(), depositInitDto.getCurrency(),
                WalletStatus.ACTIVE).orElseThrow(() -> new WalletNotFoundOrBlockedException(
                String.format("Wallet with id = [%s] not found or blocked", depositInitDto.getWalletUuid())
        ));
        log.info("Wallet with id = [{}] found", depositInitDto.getWalletUuid());
        var commission = commissionService.calculateCommission(depositInitDto.getAmount());
        var token = tokenService.generateTransactionTokenForDepositOrWithdrawal(depositInitDto.getWalletUuid(),
                depositInitDto.getAmount(), depositInitDto.getCurrency());
        log.info("Deposit init for wallet with id = [{}] success", depositInitDto.getWalletUuid());
        return new InitResponseDto(token,commission);
    }

    public InitResponseDto initWithdrawal(WithdrawalInitDto withdrawalInitDto) {
        return null;
    }

    public InitResponseDto initTransfer(TransferInitDto transferInitDto) {
        return null;
    }
}
