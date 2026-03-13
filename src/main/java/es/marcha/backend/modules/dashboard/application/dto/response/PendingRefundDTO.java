package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para reembolsos pendientes de procesar (rol ORDERS).
 * <p>
 * Representa un pago que requiere procesamiento de reembolso.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRefundDTO {

    /**
     * ID del pago.
     */
    private long paymentId;

    /**
     * ID del pedido asociado.
     */
    private long orderId;

    /**
     * Monto a reembolsar.
     */
    private BigDecimal amount;

    /**
     * Método de pago original.
     */
    private String paymentMethod;

    /**
     * Fecha de creación del pedido.
     */
    private LocalDateTime orderCreatedAt;

    /**
     * Nombre completo del cliente.
     */
    private String customerName;

    /**
     * Email del cliente.
     */
    private String customerEmail;

    /**
     * ID de Stripe (si aplica).
     */
    private String stripePaymentIntentId;
}
