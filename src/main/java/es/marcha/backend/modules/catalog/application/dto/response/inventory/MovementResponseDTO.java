package es.marcha.backend.modules.catalog.application.dto.response.inventory;

import java.time.LocalDateTime;

import es.marcha.backend.modules.catalog.domain.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta con los datos de un movimiento de inventario.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MovementResponseDTO {
    private long id;
    private long productId;
    private String productName;
    private String productSku;
    /**
     * Cantidad del movimiento (siempre positiva; el tipo indica si es entrada o
     * salida)
     */
    private int quantity;
    /** Stock antes del movimiento */
    private int previousQuantity;
    /** Stock después del movimiento */
    private int newQuantity;
    /** Tipo de movimiento */
    private MovementType movementType;
    /** Notas opcionales del movimiento */
    private String notes;
    /** Usuario o proceso que generó el movimiento */
    private String createdBy;
    private LocalDateTime createdAt;
}
