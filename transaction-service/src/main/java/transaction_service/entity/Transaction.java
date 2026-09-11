package transaction_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import transaction_service.entity.enums.PaymentType;
import transaction_service.entity.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", schema = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @ColumnDefault("now() AT TIME ZONE 'UTC'::text")
    private Instant createdAt;

    @NotNull
    @Column(name = "modified_at")
    @ColumnDefault("now() AT TIME ZONE 'UTC'::text")
    private Instant modifiedAt;

    @NotNull
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_uuid", nullable = false)
    private Wallet wallet;

    @NotNull
    @Column(name = "amount", nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransactionStatus status;

    @Column(name = "comment", length = 256)
    private String comment;

    @NotNull
    @Column(name = "fee", nullable = false)
    private BigDecimal fee;

    @Column(name = "target_wallet_uid")
    private UUID targetWalletUid;

    @Column(name = "failure_reason", length = 256)
    private String failureReason;
}