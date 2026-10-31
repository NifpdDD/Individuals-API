package transaction_service.service.transaction;

import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionStatusResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.dto.*;
import transaction_service.entity.Transaction;
import transaction_service.entity.Wallet;
import transaction_service.entity.enums.TransactionStatus;
import transaction_service.entity.enums.WalletStatus;
import transaction_service.exceptions.NotEnoughMoneyException;
import transaction_service.exceptions.TransactionNotFound;
import transaction_service.exceptions.WalletNotFoundOrBlockedException;
import transaction_service.factory.OutboxFactory;
import transaction_service.factory.TransactionFactory;
import transaction_service.repository.TransactionRepository;
import transaction_service.repository.WalletRepository;
import transaction_service.service.statemachine.TransactionStateMachine;
import transaction_service.service.wallet.WalletService;
import transaction_service.util.DateTimeUtil;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    public static final String FAILURE_REASON = "Кошелек пользователя заблокирован";
    private final WalletFinalizeService walletFinalizeService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final DateTimeUtil dateTimeUtil;
    private final TransactionFailWriter transactionFailWriter;
    private final TransactionFactory transactionFactory;
    private final OutboxFactory outboxFactory;
    private final Tracer tracer;

    @Transactional(readOnly = true)
    public TransactionStatusResponse getStatusById(UUID transactionId) {
        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFound(
                        String.format("Transaction with ID %s not found", transactionId)
                ));

        return new TransactionStatusResponse(transaction.getStatus().toString());
    }
    @Transactional
    public TransactionConfirmResponse confirmTransfer(UUID transactionId, ValidatedTransferDto transferDto) {
        Transaction transaction = transactionFactory.createTransferTransaction(transactionId, transferDto);
        UUID fromId = transferDto.fromWalletUuid();
        UUID toId = transferDto.toWalletUuid();
        try {
            if (fromId.equals(toId)) {
                throw new IllegalArgumentException("Cannot transfer money to the same wallet");
            }
            Wallet walletFrom = walletService.getWalletForUpdate(transferDto.fromWalletUuid(),transactionId);
            validateWalletStatus(transactionId, walletFrom, "Sender");
            Wallet walletTo = walletService.getWalletForUpdate(transferDto.toWalletUuid(),transactionId);
            validateWalletStatus(transactionId, walletTo, "Receiver");
            executeTransfer(walletFrom, walletTo, transferDto.amount(), transactionId);
            transactionRepository.save(transaction);
            log.info("Transfer [{}] successful. From wallet [{}] to wallet [{}]", transactionId, fromId, toId);
            return new TransactionConfirmResponse(transactionId, TransactionStatus.SUCCESS.name());
        } catch (RuntimeException ex) {
            log.warn("Transfer [{}] failed due to business rule: {}", transactionId, ex.getMessage());
            transactionFailWriter.logFailure(transaction, ex.getMessage());
            throw ex;
        }
    }


    @Transactional
    public TransactionConfirmResponse confirmDeposit(UUID transactionId, ValidatedDepositDto depositDto) {
        Transaction transaction = transactionFactory.createDepositTransaction(transactionId, depositDto);
        UUID walletId = depositDto.walletUuid();
        String traceId = getTrace();
        try {
            Wallet wallet = walletService.getWalletForUpdate(depositDto.walletUuid(),transactionId);
            validateWalletStatus(transactionId, wallet, "Receiver");
            transactionRepository.save(transaction);
            var transactionEvent = TransactionEventDto.builder().amount(depositDto.amount()).walletId(depositDto.walletUuid()).
                    transactionId(transactionId).userId(depositDto.userUUID()).currency(depositDto.currency()).timestamp(dateTimeUtil.now())
                   .build();
            outboxFactory.saveEvent(
                    transactionEvent,
                    traceId
            );
            log.info("Deposit [{}] successful for wallet [{}]. Amount added: {}.", transactionId, walletId, depositDto.amount());
            return new TransactionConfirmResponse(transactionId, TransactionStatus.PENDING.name());
        } catch (RuntimeException ex) {
            log.warn("Deposit [{}] failed due to business rule: {}", transactionId, ex.getMessage());
            transactionFailWriter.logFailure(transaction, ex.getMessage());
            throw ex;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public TransactionConfirmResponse confirmWithdrawal(UUID transactionId, ValidatedWithdrawalDto withdrawalDto) {
        String traceId = getTrace();
        Transaction transaction = transactionFactory.createWithdrawalTransaction(transactionId, withdrawalDto);
        UUID walletId = withdrawalDto.walletUuid();
        try {
            Wallet wallet =  walletService.getWalletForUpdate(withdrawalDto.walletUuid(),transactionId);
            validateWalletStatus(transactionId, wallet, "Sender");
            if (wallet.getBalance().compareTo(withdrawalDto.amount()) < 0) {
                throw new NotEnoughMoneyException(transactionId, "Insufficient funds in wallet for withdrawal: " + walletId);
            }
            wallet.setBalance(wallet.getBalance().subtract(withdrawalDto.amount()));
            transactionRepository.save(transaction);
            var transactionEvent = TransactionEventDto.builder().amount(withdrawalDto.amount()).walletId(withdrawalDto.walletUuid()).
                    transactionId(transactionId).userId(withdrawalDto.userUuid()).currency(withdrawalDto.currency()).timestamp(dateTimeUtil.now())
                    .destination("pivo")
                    .build();
            outboxFactory.saveEvent(
                    transactionEvent,
                    traceId
            );
            log.info("Withdrawal [{}] successful from wallet [{}]. Amount subtracted: {}.", transactionId, walletId, withdrawalDto.amount());
            return new TransactionConfirmResponse(transactionId, TransactionStatus.PENDING.name());
        } catch (RuntimeException ex) {
            log.warn("Withdrawal [{}] failed due to business rule: {}", transactionId, ex.getMessage());
            transactionFailWriter.logFailure(transaction, ex.getMessage());
            throw ex;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void processTransactionUpdate(ExternalTransactionResponseDto event) {
        UUID txId = event.getTransactionId();
        Transaction existingTx = transactionRepository.findById(txId).orElse(null);
        if (existingTx == null) {
            log.warn("Транзакция с UUID {} не найдена в БД. Обновление пропущено.", txId);
            return;
        }

        TransactionStatus targetStatus = event.getStatus().toTransactionStatus();
        TransactionStateMachine stateMachine = TransactionStateMachine.getByPaymentType(existingTx.getType());
        stateMachine.transition(
                existingTx.getStatus(),
                targetStatus,
                walletFinalizeService,
                existingTx.getWalletUuid(),
                existingTx.getAmount()
        );
        transactionRepository.updateTransactionStatus(
                txId,
                targetStatus,
                event.getTimestamp(),
                event.getFailureReason()
        );
        log.info("Статус транзакции {} успешно обновлен в БД на {}", txId, targetStatus);
    }

    private void executeTransfer(Wallet from, Wallet to,BigDecimal amount, UUID transactionID) {
        if (from.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughMoneyException(transactionID, "Insufficient funds in sender wallet: " + from.getUuid());
        }
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }

    private void validateWalletStatus(UUID transactionID, Wallet wallet, String role) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotFoundOrBlockedException(transactionID, role + " wallet is not active. Current status: " + wallet.getStatus());
        }
    }

    private @NonNull String getTrace() {
        return Objects.requireNonNull(tracer.currentTraceContext().context()).traceId();
    }
}


