package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para productos mejor valorados (rol STORE).
 * <p>
 * Representa un producto con sus valoraciones promedio y número de reviews.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestRatedProductDTO {

    /**
     * ID del producto.
     */
    private long productId;

    /**
     * Nombre del producto.
     */
    private String name;

    /**
     * SKU del producto.
     */
    private String sku;

    /**
     * Precio actual.
     */
    private BigDecimal price;

    /**
     * Valoración promedio (1-5).
     */
    private Double averageRating;

    /**
     * Número total de reviews.
     */
    private long reviewCount;

    /**
     * URL de la imagen principal.
     */
    private String imageUrl;

    /**
     * Stock actual.
     */
    private int stock;
}
