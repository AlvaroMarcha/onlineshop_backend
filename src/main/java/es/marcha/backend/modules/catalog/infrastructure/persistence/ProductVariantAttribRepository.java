package es.marcha.backend.modules.catalog.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.model.product.ProductVariantAttrib;

@Repository
public interface ProductVariantAttribRepository extends JpaRepository<ProductVariantAttrib, Long> {

    List<ProductVariantAttrib> findAllByVariantId(long variantId);

    boolean existsByVariantIdAndAttribValueAttribId(long variantId, long attribId);
}
