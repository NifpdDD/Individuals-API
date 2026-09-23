package transaction_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transaction_service.entity.WalletType;

import java.util.UUID;

public interface WalletTypeRepository extends JpaRepository<WalletType, UUID> {
}
