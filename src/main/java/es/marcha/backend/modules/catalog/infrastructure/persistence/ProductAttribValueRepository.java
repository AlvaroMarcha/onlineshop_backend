package es.marcha.backend.modules.catalog.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.model.product.ProductAttribValue;

@Repository
public interface ProductAttribValueRepository extends JpaRepository<ProductAttribValue, Long> {

    List<ProductAttribValue> findAllByAttribId(long attribId);

    List<ProductAttribValue> findAllByAttribIdAndIsActiveTrue(long attribId);
}
