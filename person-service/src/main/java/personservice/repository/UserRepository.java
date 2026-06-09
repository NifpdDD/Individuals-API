package personservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personservice.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
