package es.marcha.backend.repository.ecommerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.product.ProductAttrib;

@Repository
public interface ProductAttribRepository extends JpaRepository<ProductAttrib, Long> {

    Optional<ProductAttrib> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
