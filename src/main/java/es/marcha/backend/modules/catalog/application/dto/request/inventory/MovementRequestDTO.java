package es.marcha.backend.modules.catalog.application.dto.request.inventory;

import es.marcha.backend.modules.catalog.domain.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para registrar un movimiento de inventario manual.
 * Se usa para ajustes, reposiciones, devoluciones, etc.
 */
@NoArgsConstructor
@Getter
@Setter
public class MovementRequestDTO {
    /**
     * Cantidad a mover. Siempre positiva.
     * El tipo de movimiento determina si se suma o se resta del stock.
     */
    @NotNull
    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    private int quantity;

    /**
     * Tipo de movimiento: IN, OUT, ADJUSTMENT, RESTOCK, RETURN, SALE, PURCHASE.
     * Los tipos IN, RESTOCK, PURCHASE, RETURN suman al stock.
     * Los tipos OUT, SALE restan del stock.
     * ADJUSTMENT puede ser positivo (suma) o negativo (resta) → usar quantity
     * negativa no está
     * permitido; para ajuste negativo usar quantity positiva con tipo OUT.
     */
    @NotNull
    private MovementType movementType;

    /** Notas opcionales (ej: "Recepción albarán #123", "Devolución pedido #456") */
    private String notes;

    /** Quién realiza el movimiento (nombre del admin o proceso) */
    private String createdBy;
}
