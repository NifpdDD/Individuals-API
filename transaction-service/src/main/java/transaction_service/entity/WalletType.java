package transaction_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import transaction_service.entity.enums.WalletStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_types", schema = "transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @NotNull
    @ColumnDefault("(now() AT TIME ZONE 'utc')")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    @NotNull
    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @NotNull
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @NotNull
    @Column(name = "status", nullable = false, length = 18)
    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "user_type", length = 15)
    private String userType;

    @Column(name = "creator")
    private String creator;

    @Column(name = "modifier")
    private String modifier;
}