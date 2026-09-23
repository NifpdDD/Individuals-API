package transaction_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.entity.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query("SELECT e FROM OutboxEvent e WHERE e.processed = false ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnprocessedEvents(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.processed = true WHERE e.uuid IN :uuids")
    void markAsProcessed(@Param("uuids") List<UUID> uuids);
}
