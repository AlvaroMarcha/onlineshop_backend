package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para facturas recientes.
 * <p>
 * Representa una factura generada recientemente en el sistema.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentInvoiceDTO {

    /**
     * ID de la factura.
     */
    private long invoiceId;

    /**
     * Número de factura correlativo (ej: INV-2026-000001).
     */
    private String invoiceNumber;

    /**
     * ID del pedido asociado.
     */
    private long orderId;

    /**
     * Monto total de la factura.
     */
    private BigDecimal totalAmount;

    /**
     * Fecha de emisión de la factura.
     */
    private LocalDateTime createdAt;

    /**
     * ID del usuario destinatario.
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
}
