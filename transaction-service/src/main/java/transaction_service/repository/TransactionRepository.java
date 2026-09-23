package transaction_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import transaction_service.entity.Transaction;
import transaction_service.entity.enums.TransactionStatus;

import java.time.Instant;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Modifying
    @Query("""
        UPDATE Transaction t 
        SET t.status = :status, 
            t.modifiedAt = :modifiedAt, 
            t.failureReason = :failureReason 
        WHERE t.uuid = :uuid
    """)
    int updateTransactionStatus(
            @Param("uuid") UUID uuid,
            @Param("status") TransactionStatus status,
            @Param("modifiedAt") Instant modifiedAt,
            @Param("failureReason") String failureReason
    );
}
