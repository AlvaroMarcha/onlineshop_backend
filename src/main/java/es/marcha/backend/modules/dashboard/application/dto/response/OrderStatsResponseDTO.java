package es.marcha.backend.modules.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para las estadísticas de pedidos por estado.
 * <p>
 * Contiene el número de pedidos en cada estado del sistema.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatsResponseDTO {

    /**
     * Total de pedidos en el sistema.
     */
    private long total;

    /**
     * Pedidos en estado CREATED (creados, esperando pago).
     */
    private long pending;

    /**
     * Pedidos en estado PAID (pago confirmado).
     */
    private long paid;

    /**
     * Pedidos en estado PROCESSING (en preparación).
     */
    private long processing;

    /**
     * Pedidos en estado SHIPPED (enviados).
     */
    private long shipped;

    /**
     * Pedidos en estado DELIVERED (entregados).
     */
    private long delivered;

    /**
     * Pedidos en estado CANCELLED (cancelados).
     */
    private long cancelled;

    /**
     * Pedidos en estado RETURNED (devueltos/reembolsados).
     */
    private long returned;
}
