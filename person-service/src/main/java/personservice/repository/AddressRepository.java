package personservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import personservice.entity.Address;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    @Modifying
    @Query("""
            UPDATE Individual i SET i.active = false WHERE i.id = :id
            """)
    void softDelete(@Param("id") UUID id);
}
