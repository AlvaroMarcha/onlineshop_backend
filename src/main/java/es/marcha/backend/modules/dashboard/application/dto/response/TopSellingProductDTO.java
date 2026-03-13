package es.marcha.backend.modules.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el producto más vendido.
 * <p>
 * Contiene información básica del producto y su cantidad vendida.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductDTO {

    /**
     * ID del producto.
     */
    private long productId;

    /**
     * Nombre del producto.
     */
    private String productName;

    /**
     * Slug del producto (para URL).
     */
    private String slug;

    /**
     * Cantidad total vendida (soldCount).
     */
    private int soldCount;

    /**
     * Stock actual del producto.
     */
    private int currentStock;

    /**
     * URL de la imagen principal del producto.
     */
    private String imageUrl;
}
