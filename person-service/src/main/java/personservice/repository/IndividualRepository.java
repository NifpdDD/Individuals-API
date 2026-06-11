package personservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import personservice.entity.Individual;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IndividualRepository extends JpaRepository<Individual, UUID> {

    @Query(
            value = "FROM Individual i WHERE i.active = true AND(:emails IS NULL OR i.user.email IN:emails)",
            countQuery = "SELECT COUNT(i) FROM Individual i WHERE i.active = true AND(:emails IS NULL OR i.user.email IN:emails)"
    )
    Page<Individual> findAllByEmails(@Param("emails") List<String> emails, Pageable pageable);

    Optional<Individual> findByIdAndActiveIsTrue(UUID id);

    @Modifying
    @Query("""
            UPDATE Individual i SET i.active = false WHERE i.id = :id
            """)
    void softDelete(@Param("id") UUID id);
}
