package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el valor medio del pedido (Average Order Value - AOV).
 * <p>
 * Calcula el importe medio de los pedidos completados en el sistema.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AverageOrderValueResponseDTO {

    /**
     * Valor medio del pedido.
     * Calculado como: totalRevenue / totalOrders
     */
    private BigDecimal averageOrderValue;

    /**
     * Ingresos totales de todos los pedidos completados.
     */
    private BigDecimal totalRevenue;

    /**
     * Total de pedidos completados (estados PAID o superiores).
     */
    private long totalOrders;
}
