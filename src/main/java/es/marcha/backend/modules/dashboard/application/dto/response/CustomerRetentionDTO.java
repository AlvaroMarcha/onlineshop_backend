package es.marcha.backend.modules.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas de retención de clientes (rol CUSTOMERS).
 * <p>
 * Contiene porcentajes de clientes recurrentes y métricas de retención.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRetentionDTO {

    /**
     * Total de clientes registrados.
     */
    private long totalCustomers;

    /**
     * Clientes con al menos un pedido.
     */
    private long customersWithOrders;

    /**
     * Clientes con más de un pedido (recurrentes).
     */
    private long recurringCustomers;

    /**
     * Porcentaje de retención (clientes con más de un pedido / total con pedidos).
     */
    private Double retentionRate;

    /**
     * Porcentaje de conversión (clientes con pedidos / total).
     */
    private Double conversionRate;
}
