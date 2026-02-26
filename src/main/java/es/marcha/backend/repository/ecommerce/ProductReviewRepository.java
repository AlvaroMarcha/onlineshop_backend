package es.marcha.backend.repository.ecommerce;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.ProductReview;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    @Query("SELECT r FROM ProductReview r WHERE r.product.id = :productId")
    List<ProductReview> findAllByProductId(@Param("productId") long productId);
}