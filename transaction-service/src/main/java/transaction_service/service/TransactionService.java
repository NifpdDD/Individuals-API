package transaction_service.service;

import com.example.transaction.dto.TransactionConfirmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.entity.Transaction;
import transaction_service.entity.Wallet;
import transaction_service.entity.enums.PaymentType;
import transaction_service.entity.enums.TransactionStatus;
import transaction_service.entity.enums.WalletStatus;
import transaction_service.exceptions.NotEnoughMoneyException;
import transaction_service.exceptions.WalletNotFoundOrBlockedException;
import transaction_service.factory.TransactionFactory;
import transaction_service.model.TransactionEvent;
import transaction_service.model.ValidatedDepositDto;
import transaction_service.model.ValidatedTransferDto;
import transaction_service.model.ValidatedWithdrawalDto;
import transaction_service.repository.TransactionRepository;
import transaction_service.repository.WalletRepository;
import transaction_service.util.DateTimeUtil;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    public static final String FAILURE_REASON = "Кошелек пользователя заблокирован";
    private final WalletFinalizeService walletFinalizeService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final DateTimeUtil dateTimeUtil;
    private final TransactionWriter transactionWriter;
    private final TransactionFactory transactionFactory;
    private final OutboxService outboxService;

    @Transactional
    public TransactionConfirmResponse confirmTransfer(UUID transactionId, ValidatedTransferDto transferDto) {
        Transaction transaction = transactionFactory.createTransferTransaction(transactionId, transferDto);
        UUID fromId = transferDto.fromWalletUuid();
        UUID toId = transferDto.toWalletUuid();
        try {
            if (fromId.equals(toId)) {
                throw new IllegalArgumentException("Cannot transfer money to the same wallet");
            }
            WalletPair pair = lockWalletsInOrder(transactionId, fromId, toId);
            validateWalletStatus(transactionId, pair.from(), "Sender");
            validateWalletStatus(transactionId, pair.to(), "Receiver");
            pair.executeTransfer(transferDto.amount(), transactionId);
            transactionRepository.save(transaction);
            log.info("Transfer [{}] successful. From wallet [{}] to wallet [{}]", transactionId, fromId, toId);
            return new TransactionConfirmResponse(transactionId, TransactionStatus.SUCCESS.name());
        } catch (RuntimeException ex) {
            log.warn("Transfer [{}] failed due to business rule: {}", transactionId, ex.getMessage());
            transactionWriter.logFailure(transaction, ex.getMessage());
            throw ex;
        }
    }

    private WalletPair lockWalletsInOrder(UUID transactionID, UUID fromId, UUID toId) {
        boolean isFromFirst = fromId.compareTo(toId) < 0;
        UUID firstId = isFromFirst ? fromId : toId;
        UUID secondId = isFromFirst ? toId : fromId;
        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new WalletNotFoundOrBlockedException(transactionID, "Wallet not found with ID: " + firstId));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new WalletNotFoundOrBlockedException(transactionID, "Wallet not found with ID: " + secondId));
        return new WalletPair(firstWallet, secondWallet);
    }

    private void validateWalletStatus(UUID transactionID, Wallet wallet, String role) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotFoundOrBlockedException(transactionID, role + " wallet is not active. Current status: " + wallet.getStatus());
        }
    }

    @Transactional
    public TransactionConfirmResponse confirmDeposit(UUID transactionId, ValidatedDepositDto depositDto) {
        Transaction transaction = transactionFactory.createDepositTransaction(transactionId, depositDto);
        UUID walletId = depositDto.walletUuid();
        try {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundOrBlockedException(transactionId, "Wallet not found with ID: " + walletId));
            if (wallet.getStatus() != WalletStatus.ACTIVE) {
                throw new WalletNotFoundOrBlockedException(transactionId, "Target wallet is not active. Current status: " + wallet.getStatus());
            }
            transactionRepository.save(transaction);
            outboxService.saveDepositEvent(transactionId,
                    transaction.getUserUuid(),
                    walletId,
                    depositDto.amount(),
                    depositDto.currency(),
                    dateTimeUtil.now());
            log.info("Deposit [{}] successful for wallet [{}]. Amount added: {}.", transactionId, walletId, depositDto.amount());
            return new TransactionConfirmResponse(transactionId, TransactionStatus.PENDING.name());
        } catch (RuntimeException ex) {
            log.warn("Deposit [{}] failed due to business rule: {}", transactionId, ex.getMessage());
            transactionWriter.logFailure(transaction, ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public TransactionConfirmResponse confirmWithdrawal(UUID transactionId, ValidatedWithdrawalDto withdrawalDto) {
        Transaction transaction = transactionFactory.createWithdrawalTransaction(transactionId, withdrawalDto);
        UUID walletId = withdrawalDto.walletUuid();
        try {
            Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                    .orElseThrow(() -> new WalletNotFoundOrBlockedException(transactionId, "Wallet not found with ID: " + walletId));
            if (wallet.getStatus() != WalletStatus.ACTIVE) {
                throw new WalletNotFoundOrBlockedException(transactionId, "Source wallet is not active. Current status: " + wallet.getStatus());
            }
            if (wallet.getBalance().compareTo(withdrawalDto.amount()) < 0) {
                throw new NotEnoughMoneyException(transactionId, "Insufficient funds in wallet for withdrawal: " + walletId);
            }
            wallet.setBalance(wallet.getBalance().subtract(withdrawalDto.amount()));
            transactionRepository.save(transaction);
            outboxService.saveWithdrawalEvent(
                    transactionId,
                    transaction.getUserUuid(),
                    walletId,
                    withdrawalDto.amount(),
                    withdrawalDto.currency(),
                    "Visa **** 1234",
                    dateTimeUtil.now()
            );

            log.info("Withdrawal [{}] successful from wallet [{}]. Amount subtracted: {}.", transactionId, walletId, withdrawalDto.amount());
            return new TransactionConfirmResponse(transactionId, TransactionStatus.PENDING.name());
        } catch (RuntimeException ex) {
            log.warn("Withdrawal [{}] failed due to business rule: {}", transactionId, ex.getMessage());
            transactionWriter.logFailure(transaction, ex.getMessage());
            throw ex;
        }
    }

    private record WalletPair(Wallet from, Wallet to) {

        public void executeTransfer(BigDecimal amount, UUID transactionID) {
            if (from.getBalance().compareTo(amount) < 0) {
                throw new NotEnoughMoneyException(transactionID, "Insufficient funds in sender wallet: " + from.getUuid());
            }
            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));
        }
    }

    @Transactional
    public void processTransactionUpdate(TransactionEvent event) {
        UUID txId = event.getTransactionId();
        Transaction existingTx = transactionRepository.findById(txId).orElse(null);
        if (existingTx == null) {
            log.warn("Транзакция с UUID {} не найдена в БД. Обновление пропущено.", txId);
            return;
        }
        UUID walletId = existingTx.getWalletUuid();
        if (walletRepository.getWalletStatus(walletId).equals(WalletStatus.BLOCKED)) {
            log.warn("Кошелек {} заблокирован. Транзакция {} отменяется.", walletId, txId);
            transactionRepository.updateTransactionStatus(
                    txId,
                    TransactionStatus.FAILED,
                    event.getTimestamp(),
                    FAILURE_REASON
            );
        }

        if (!TransactionStatus.PENDING.equals(existingTx.getStatus())) {
            log.warn("Игнорируем сообщение. Транзакция {} уже в финальном статусе: {}.", txId, existingTx.getStatus());
            return;
        }
        transactionRepository.updateTransactionStatus(
                txId,
                event.getStatus(),
                event.getTimestamp(),
                event.getFailureReason()
        );
        log.info("Статус транзакции {} успешно обновлен в БД на {}", txId, event.getStatus());
        finalizeWalletBalance(existingTx, event);
    }

    private void finalizeWalletBalance(Transaction tx, TransactionEvent event) {
        TransactionStatus newStatus = event.getStatus();
        PaymentType type = tx.getType();
        UUID walletId = tx.getWalletUuid();
        if (TransactionStatus.FAILED.equals(newStatus) && PaymentType.WITHDRAWAL.equals(type)) {
            log.info("Запуск процесса возврата средств на депозит для транзакции UUID={}", event.getTransactionId());
            walletFinalizeService.refundWithdrawal(walletId, event.getAmount());
            log.info("Средства для транзакции {} успешно возвращены на депозит", event.getTransactionId());
        }

        else if (TransactionStatus.SUCCESS.equals(newStatus) && PaymentType.DEPOSIT.equals(type)) {
            log.info("Запуск процесса пополнения средств с депозита для транзакции UUID={}", event.getTransactionId());
            walletFinalizeService.completeDeposit(walletId, event.getAmount());
            log.info("Средства для транзакции {} успешно зачислены", event.getTransactionId());
        }
    }

}


