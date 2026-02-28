package es.marcha.backend.repository.ecommerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.product.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findAllByProductId(long productId);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(long productId);

    boolean existsBySku(String sku);
}
