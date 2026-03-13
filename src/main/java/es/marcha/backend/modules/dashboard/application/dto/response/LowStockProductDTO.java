package es.marcha.backend.modules.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para productos con stock bajo.
 * <p>
 * Contiene información del producto cuyo stock está por debajo del umbral
 * especificado.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockProductDTO {

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
     * Stock actual del producto.
     */
    private int currentStock;

    /**
     * Cantidad vendida (soldCount). Indica la demanda del producto.
     */
    private int soldCount;

    /**
     * Estado del producto (activo/inactivo).
     */
    private boolean isActive;

    /**
     * URL de la imagen principal del producto.
     */
    private String imageUrl;
}
