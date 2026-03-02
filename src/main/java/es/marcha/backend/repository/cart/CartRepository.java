package es.marcha.backend.repository.cart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.cart.Cart;
import es.marcha.backend.model.enums.CartStatus;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /** Busca el carrito activo de un usuario */
    Optional<Cart> findByUserIdAndStatus(long userId, CartStatus status);

    /** Carritos activos cuya expiración ya ha pasado — usados por el scheduler */
    List<Cart> findAllByStatusAndExpiresAtBefore(CartStatus status, LocalDateTime now);
}
