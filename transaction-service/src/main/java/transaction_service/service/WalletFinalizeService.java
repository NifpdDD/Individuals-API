package transaction_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public  class WalletFinalizeService {
    private final WalletRepository walletRepository;
    /**
     * Возврат заблокированных средств на депозит пользователя (в случае FAILED)
     */
    @Transactional // Открывает транзакцию БД, которая удерживает блокировку FOR UPDATE
    public void refundWithdrawal(UUID walletId, BigDecimal amount) {
        log.info("Запрос SELECT FOR UPDATE для возврата на кошелек: {}", walletId);
        var wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Кошелек не найден: " + walletId));
        var newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        log.info("Возврат средств для депозита в результате ошибки операции. Кошелек: {}",
                walletId);
    }

    /**
     * Окончательное зачисление / пополнение баланса (в случае COMPLETED)
     */
    @Transactional
    void completeDeposit(UUID walletID,BigDecimal amount){
        var wallet = walletRepository.findByIdForUpdate(walletID)
                .orElseThrow(() -> new IllegalArgumentException("Кошелек не найден: " + walletID));
        var newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        log.info("Успешное поплнение. Кошелек: {}",
                walletID);

    }
}