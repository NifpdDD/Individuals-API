package transaction_service.service.statemachine;

import transaction_service.entity.enums.TransactionStatus;

import java.util.Set;

public record TransitionConfig(
        Set<TransactionStatus> allowedSourceStatuses,
        WalletAction walletAction
) {}