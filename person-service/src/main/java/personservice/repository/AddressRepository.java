package personservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personservice.entity.Address;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
