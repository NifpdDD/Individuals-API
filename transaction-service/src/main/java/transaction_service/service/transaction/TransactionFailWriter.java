package transaction_service.service.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.entity.Transaction;
import transaction_service.entity.enums.TransactionStatus;
import transaction_service.repository.TransactionRepository;

@Component
@RequiredArgsConstructor
class TransactionFailWriter {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(Transaction transaction, String failureReason) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(failureReason);
        transactionRepository.save(transaction);
    }
}