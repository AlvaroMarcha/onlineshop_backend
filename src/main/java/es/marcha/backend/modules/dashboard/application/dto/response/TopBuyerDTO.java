package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para clientes que más han gastado (rol CUSTOMERS).
 * <p>
 * Representa un cliente VIP con su gasto total.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopBuyerDTO {

    /**
     * ID del usuario.
     */
    private long userId;

    /**
     * Nombre completo.
     */
    private String name;

    /**
     * Apellido.
     */
    private String surname;

    /**
     * Email.
     */
    private String email;

    /**
     * Gasto total acumulado.
     */
    private BigDecimal totalSpent;

    /**
     * Número de pedidos realizados.
     */
    private long orderCount;

    /**
     * Gasto promedio por pedido.
     */
    private BigDecimal averageOrderValue;
}
