package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para envíos con retraso (rol ORDERS).
 * <p>
 * Representa pedidos que están retrasados respecto a su fecha estimada de
 * entrega.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelayedShipmentDTO {

    /**
     * ID del pedido.
     */
    private long orderId;

    /**
     * Estado actual del pedido.
     */
    private String status;

    /**
     * Monto total del pedido.
     */
    private BigDecimal totalAmount;

    /**
     * Fecha de creación del pedido.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha estimada de entrega (calculada: +7 días desde creación).
     */
    private LocalDateTime estimatedDeliveryDate;

    /**
     * Días de retraso.
     */
    private long daysDelayed;

    /**
     * Nombre completo del cliente.
     */
    private String customerName;

    /**
     * Email del cliente.
     */
    private String customerEmail;

    /**
     * Dirección de envío.
     */
    private String shippingAddress;
}
