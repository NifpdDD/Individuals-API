package transaction_service.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import transaction_service.entity.Transaction;
import transaction_service.entity.enums.PaymentType;
import transaction_service.entity.enums.TransactionStatus;
import transaction_service.model.ValidatedDepositDto;
import transaction_service.model.ValidatedTransferDto;
import transaction_service.model.ValidatedWithdrawalDto;
import transaction_service.util.DateTimeUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionFactory {

    private final DateTimeUtil dateTimeUtil;

    public Transaction createTransferTransaction(UUID transactionId, ValidatedTransferDto transferDto) {
        return createBaseBuilder(transactionId, transferDto.amount(), transferDto.fromWalletUuid(),transferDto.fromUserUuid(),BigDecimal.ZERO)
                .type(PaymentType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .targetWalletUuid(transferDto.toWalletUuid())
                .build();
    }

    public Transaction createDepositTransaction(UUID transactionId, ValidatedDepositDto depositDto) {
        return createBaseBuilder(transactionId, depositDto.amount(), depositDto.walletUuid(),depositDto.userUUID(), depositDto.fee())
                .type(PaymentType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .build();
    }

    public Transaction createWithdrawalTransaction(UUID transactionId, ValidatedWithdrawalDto withdrawalDto) {
        return createBaseBuilder(transactionId, withdrawalDto.amount(), withdrawalDto.walletUuid(),withdrawalDto.userUuid(),withdrawalDto.fee())
                .type(PaymentType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .build();
    }

    private Transaction.TransactionBuilder createBaseBuilder(UUID transactionId, BigDecimal amount, UUID walletUUID,
                                                             UUID userUUID, BigDecimal fee) {
        Instant now = dateTimeUtil.now();
        return Transaction.builder()
                .userUuid(userUUID)
                .uuid(transactionId)
                .walletUuid(walletUUID)
                .createdAt(now)
                .modifiedAt(now)
                .amount(amount)
                .fee(fee);
    }
}
