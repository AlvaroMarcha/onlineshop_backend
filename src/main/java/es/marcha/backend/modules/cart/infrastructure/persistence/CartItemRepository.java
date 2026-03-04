package es.marcha.backend.modules.cart.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.cart.domain.model.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCartId(long cartId);

    Optional<CartItem> findByCartIdAndId(long cartId, long id);

    /**
     * Busca un ítem existente en el carrito para el mismo producto y variante.
     * Usa JPQL para manejar correctamente el caso variant = null.
     */
    @Query("""
            SELECT ci FROM CartItem ci
            WHERE ci.cart.id = :cartId
              AND ci.product.id = :productId
              AND ((:variantId IS NULL AND ci.variant IS NULL)
                OR ci.variant.id = :variantId)
            """)
    Optional<CartItem> findByCartIdAndProductIdAndVariantId(
            @Param("cartId") long cartId,
            @Param("productId") long productId,
            @Param("variantId") Long variantId);
}
