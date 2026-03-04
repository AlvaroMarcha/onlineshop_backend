package es.marcha.backend.modules.cart.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.cart.domain.model.Cart;
import es.marcha.backend.core.shared.domain.enums.CartStatus;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /** Busca el carrito activo de un usuario */
    Optional<Cart> findByUserIdAndStatus(long userId, CartStatus status);

    /** Carritos activos cuya expiración ya ha pasado — usados por el scheduler */
    List<Cart> findAllByStatusAndExpiresAtBefore(CartStatus status, LocalDateTime now);
}
