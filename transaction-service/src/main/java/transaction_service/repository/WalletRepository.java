package transaction_service.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import transaction_service.entity.Wallet;
import transaction_service.entity.enums.WalletStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("""
        SELECT w 
        FROM Wallet w 
        JOIN FETCH w.walletType wt
        WHERE w.uuid = :walletId 
          AND w.userUuid = :userId 
          AND w.status = :status 
          AND w.archivedAt IS NULL 
          AND wt.currencyCode = :currency 
          AND wt.status = :status
    """)
    Optional<Wallet> findWalletByUserIdAndWalletTypeCurrencyCodeAndStatus(
            @Param("walletId") UUID walletId,
            @Param("userId") UUID userId,
            @Param("currency") String currency,
            @Param("status") WalletStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") // 3000 мс = 3 секунды
    })
    @Query("SELECT w FROM Wallet w WHERE w.uuid = :walletId")
    Optional<Wallet> findByIdForUpdate(@Param("walletId") UUID walletId);

    @Query("SELECT w.status FROM Wallet w WHERE w.uuid = :walletUuid")
    WalletStatus getWalletStatus(@Param("walletUuid") UUID walletUuid);
}
