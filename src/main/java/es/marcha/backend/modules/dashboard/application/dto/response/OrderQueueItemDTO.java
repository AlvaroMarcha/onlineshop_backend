package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para un elemento de la cola de pedidos pendientes (rol
 * ORDERS).
 * <p>
 * Representa un pedido pendiente de procesamiento ordenado por prioridad.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderQueueItemDTO {

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
     * Número de items en el pedido.
     */
    private int itemCount;

    /**
     * Tiempo transcurrido desde la creación (en horas).
     */
    private long hoursSinceCreation;
}
