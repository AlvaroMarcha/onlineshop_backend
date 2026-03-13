package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la tasa de conversión del negocio.
 * <p>
 * Mide la proporción de usuarios que completan un pedido en relación al total
 * de usuarios registrados o visitantes.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionRateResponseDTO {

    /**
     * Total de usuarios registrados únicos.
     */
    private long totalUsers;

    /**
     * Total de usuarios que han realizado al menos un pedido.
     */
    private long usersWithOrders;

    /**
     * Tasa de conversión (porcentaje de usuarios que han comprado).
     * Calculado como: (usersWithOrders / totalUsers) * 100
     */
    private BigDecimal conversionRate;

    /**
     * Total de pedidos completados (estados PAID o superiores).
     */
    private long totalOrders;
}
