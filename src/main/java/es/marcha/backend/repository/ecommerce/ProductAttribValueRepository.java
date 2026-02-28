package es.marcha.backend.repository.ecommerce;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.product.ProductAttribValue;

@Repository
public interface ProductAttribValueRepository extends JpaRepository<ProductAttribValue, Long> {

    List<ProductAttribValue> findAllByAttribId(long attribId);

    List<ProductAttribValue> findAllByAttribIdAndIsActiveTrue(long attribId);
}
