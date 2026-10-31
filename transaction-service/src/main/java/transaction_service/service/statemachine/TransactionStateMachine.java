package transaction_service.service.statemachine;

import lombok.Getter;
import transaction_service.entity.enums.PaymentType;
import transaction_service.entity.enums.TransactionStatus;
import transaction_service.service.transaction.WalletFinalizeService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//Насколько хорошее решение?

@Getter
public enum TransactionStateMachine {

    DEPOSIT(PaymentType.DEPOSIT, Map.of(
            TransactionStatus.SUCCESS, new TransitionConfig(Set.of(TransactionStatus.PENDING), WalletFinalizeService::completeDeposit),
            TransactionStatus.FAILED,  new TransitionConfig(Set.of(TransactionStatus.PENDING), (_, _, _) -> {})
    )),

    WITHDRAWAL(PaymentType.WITHDRAWAL, Map.of(
            TransactionStatus.SUCCESS, new TransitionConfig(Set.of(TransactionStatus.PENDING), (_, _, _) -> {}),
            TransactionStatus.FAILED,  new TransitionConfig(Set.of(TransactionStatus.PENDING), WalletFinalizeService::refundWithdrawal)
    ));

    private final PaymentType paymentType;
    private final Map<TransactionStatus, TransitionConfig> transitions;

    TransactionStateMachine(PaymentType paymentType, Map<TransactionStatus, TransitionConfig> transitions) {
        this.paymentType = paymentType;
        this.transitions = transitions;
    }

    public static TransactionStateMachine getByPaymentType(PaymentType type) {
        for (TransactionStateMachine stateMachine : values()) {
            if (stateMachine.paymentType == type) {
                return stateMachine;
            }
        }
        throw new IllegalArgumentException("Неподдерживаемый тип платежа: " + type);
    }

    public void transition(
            TransactionStatus currentStatus,
            TransactionStatus targetStatus,
            WalletFinalizeService service,
            UUID walletId,
            BigDecimal amount
    ) {
        TransitionConfig config = this.transitions.get(targetStatus);
        if (config == null) {
            throw new IllegalArgumentException(
                    String.format("У автомата %s нет правил для перехода в статус %s", this.name(), targetStatus)
            );
        }
        if (!config.allowedSourceStatuses().contains(currentStatus)) {
            throw new IllegalStateException(
                    String.format("Запрещенный переход! Для операции %s нельзя перейти в статус %s из текущего статуса %s",
                            this.name(), targetStatus, currentStatus)
            );
        }

        config.walletAction().execute(service, walletId, amount);
    }
}
