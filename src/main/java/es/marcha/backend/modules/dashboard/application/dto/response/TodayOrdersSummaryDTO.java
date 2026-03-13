package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el resumen de pedidos del día (rol ORDERS).
 * <p>
 * Contiene el número total de pedidos y los ingresos totales generados en el
 * día actual.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayOrdersSummaryDTO {

    /**
     * Número total de pedidos realizados hoy.
     */
    private long totalOrders;

    /**
     * Ingresos totales generados hoy.
     */
    private BigDecimal totalRevenue;

    /**
     * Número de pedidos pendientes de procesamiento.
     */
    private long pendingOrders;

    /**
     * Número de pedidos completados hoy.
     */
    private long completedOrders;
}
