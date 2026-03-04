package es.marcha.backend.modules.wishlist.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.wishlist.domain.model.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /** Busca la wishlist de un usuario por su username */
    Optional<Wishlist> findByUserUsername(String username);

    /** Busca la wishlist de un usuario por su ID */
    Optional<Wishlist> findByUserId(long userId);
}
