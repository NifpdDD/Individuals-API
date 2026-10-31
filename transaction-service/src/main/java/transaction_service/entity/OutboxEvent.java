package transaction_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import transaction_service.dto.TransactionEventDto;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", schema = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @NotNull
    @Column(name = "user_uuid", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @NotNull
    @Column(name = "topic", nullable = false, length = 128)
    private String topic;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false,columnDefinition = "jsonb")
    private String payload;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @NotNull
    @Column(name = "trace_id", nullable = false, length = 64) // 64 символа с запасом хватит и для W3C стандартов, и для B3
    private String traceId;
}