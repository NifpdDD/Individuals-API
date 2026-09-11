package transaction_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import transaction_service.entity.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets", schema = "transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @NotNull
    @ColumnDefault("now() AT TIME ZONE 'UTC'::text")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ColumnDefault("now() AT TIME ZONE 'UTC'::text")
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @NotNull
    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_type_uuid", nullable = false)
    private WalletType walletType;

    @NotNull
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid; //Надо ли проверять что юзер существует через сервис персон?

    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @NotNull
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "archived_at")
    @ColumnDefault("now() AT TIME ZONE 'UTC'::text")
    private Instant archivedAt;
}
