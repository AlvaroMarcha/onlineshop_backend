package es.marcha.backend.repository.ecommerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.product.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Devuelve todas las imágenes de un producto ordenadas por {@code sortOrder}
     * ascendente.
     */
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    /** Devuelve la imagen principal de un producto, si existe. */
    Optional<ProductImage> findFirstByProductIdAndIsMainTrue(Long productId);

    /**
     * Devuelve la primera imagen (menor sortOrder) de un producto distinta de la
     * indicada.
     */
    Optional<ProductImage> findFirstByProductIdAndIdNotOrderBySortOrderAsc(Long productId, Long imageId);

    /** Número de imágenes que tiene un producto. */
    long countByProductId(Long productId);

    /**
     * Desactiva el flag {@code isMain} en todas las imágenes de un producto.
     * Se usa antes de establecer una nueva imagen principal.
     */
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = false WHERE pi.product.id = :productId")
    void clearMainFlagByProductId(@Param("productId") Long productId);
}
