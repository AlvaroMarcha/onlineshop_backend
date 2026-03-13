package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para productos más visitados (rol STORE).
 * <p>
 * Representa un producto con su número de vistas.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostViewedProductDTO {

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
     * Número de vistas del producto.
     */
    private long views;

    /**
     * URL de la imagen principal.
     */
    private String imageUrl;

    /**
     * Stock actual.
     */
    private int stock;
}
