package es.marcha.backend.modules.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el resumen de productos (rol STORE).
 * <p>
 * Contiene contadores de productos activos, inactivos y sin stock.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDTO {

    /**
     * Total de productos activos.
     */
    private long activeProducts;

    /**
     * Total de productos inactivos.
     */
    private long inactiveProducts;

    /**
     * Total de productos sin stock (quantity = 0).
     */
    private long outOfStockProducts;

    /**
     * Total de productos en el catálogo.
     */
    private long totalProducts;
}
