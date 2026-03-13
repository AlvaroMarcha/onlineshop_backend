package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para pedidos pendientes de gestión.
 * <p>
 * Representa un pedido en cola que requiere acción del equipo de ADMIN/ORDERS.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingOrderDTO {

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
     * Método de pago utilizado.
     */
    private String paymentMethod;

    /**
     * Fecha de creación del pedido.
     */
    private LocalDateTime createdAt;

    /**
     * ID del usuario que realizó el pedido.
     */
    private long userId;

    /**
     * Nombre completo del usuario.
     */
    private String userName;

    /**
     * Email del usuario.
     */
    private String userEmail;

    /**
     * Número de items en el pedido.
     */
    private int itemCount;
}
