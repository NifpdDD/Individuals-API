package transaction_service.service.statemachine;

import transaction_service.service.transaction.WalletFinalizeService;

import java.math.BigDecimal;
import java.util.UUID;

@FunctionalInterface
public interface WalletAction {
    void execute(WalletFinalizeService service, UUID walletId, BigDecimal amount);
}