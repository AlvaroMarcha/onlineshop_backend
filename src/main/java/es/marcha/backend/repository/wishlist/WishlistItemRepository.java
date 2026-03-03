package es.marcha.backend.repository.wishlist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.wishlist.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /** Busca un ítem concreto dentro de una wishlist por producto */
    Optional<WishlistItem> findByWishlistIdAndProductId(long wishlistId, long productId);

    /** Busca un ítem concreto en la wishlist del usuario por su ID */
    Optional<WishlistItem> findByWishlistIdAndId(long wishlistId, long id);

    /**
     * Cuenta cuántas wishlists distintas contienen el producto indicado.
     * Se usa en el endpoint público de popularidad del producto.
     */
    @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.product.id = :productId")
    long countByProductId(@Param("productId") long productId);
}
