package es.marcha.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import es.marcha.backend.model.users.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

}
