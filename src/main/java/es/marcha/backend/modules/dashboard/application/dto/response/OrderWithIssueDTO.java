package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para pedidos con incidencias o notas internas (rol SUPPORT).
 * <p>
 * Representa un pedido que requiere atención de soporte.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderWithIssueDTO {

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
     * Nombre completo del cliente.
     */
    private String customerName;

    /**
     * Email del cliente.
     */
    private String customerEmail;

    /**
     * Teléfono del cliente.
     */
    private String customerPhone;

    /**
     * Número de items en el pedido.
     */
    private int itemCount;

    /**
     * Indica si tiene pagos en estado FAILED.
     */
    private boolean hasFailedPayments;

    /**
     * Indica si tiene pagos en estado REFUNDED.
     */
    private boolean hasRefundedPayments;

    /**
     * ID del usuario para contacto.
     */
    private long userId;
}
