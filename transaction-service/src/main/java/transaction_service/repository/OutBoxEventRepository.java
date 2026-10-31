package transaction_service.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.entity.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-1")
    })
    @Query("SELECT e FROM OutboxEvent e WHERE e.processed = false ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnprocessedEventsForUpdate(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.processed = true WHERE e.uuid IN :uuids")
    void markAsProcessed(@Param("uuids") List<UUID> uuids);
}
