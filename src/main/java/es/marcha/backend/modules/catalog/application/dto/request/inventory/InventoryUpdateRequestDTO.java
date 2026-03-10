package es.marcha.backend.modules.catalog.application.dto.request.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para actualizar los metadatos del inventario de un producto.
 * No modifica directamente el stock (para eso se usa MovementRequestDTO).
 */
@NoArgsConstructor
@Getter
@Setter
public class InventoryUpdateRequestDTO {
    /** Stock mínimo antes de generar alerta de bajo stock */
    private int minStock;
    /** Stock máximo permitido en almacén (0 = sin límite) */
    private int maxStock;
    /** Unidades en camino desde proveedor */
    private int incomingStock;
    /** Unidades dañadas o no vendibles */
    private int damagedStock;
}
