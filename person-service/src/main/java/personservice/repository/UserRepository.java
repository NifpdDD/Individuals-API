package personservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import personservice.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Modifying
    @Query("""
            UPDATE User i SET i.active = false WHERE i.id = :id
            """)
    void softDelete(@Param("id") UUID id);
}
