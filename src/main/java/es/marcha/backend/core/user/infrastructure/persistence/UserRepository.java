package es.marcha.backend.core.user.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.core.user.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    Optional<User> findByVerificationToken(String verificationToken);

    /**
     * Limpia los tokens de verificación de email expirados.
     * Pone a null el token y la fecha de expiración de todos los usuarios
     * no verificados cuyo token haya superado la fecha indicada.
     *
     * @param now fecha y hora actual
     * @return número de filas actualizadas
     */
    @Modifying
    @Query("UPDATE User u SET u.verificationToken = NULL, u.verificationTokenExpiry = NULL " +
            "WHERE u.isVerified = false AND u.verificationTokenExpiry < :now")
    int clearExpiredVerificationTokens(@Param("now") LocalDateTime now);

}
