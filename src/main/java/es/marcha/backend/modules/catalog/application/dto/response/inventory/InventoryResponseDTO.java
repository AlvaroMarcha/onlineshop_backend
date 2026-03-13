package es.marcha.backend.modules.catalog.application.dto.response.inventory;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta con los datos del inventario de un producto.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class InventoryResponseDTO {
    private long id;
    private long productId;
    private String productName;
    private String productSku;
    /** Stock disponible (espejo de Product.stock) */
    private int quantity;
    /** Unidades reservadas (en pedidos pendientes de pagar) */
    private int reservedQuantity;
    /** Stock mínimo antes de alertar */
    private int minStock;
    /** Stock máximo que puede haber en almacén */
    private int maxStock;
    /** Unidades en camino (pendientes de recibir) */
    private int incomingStock;
    /** Unidades dañadas o no vendibles */
    private int damagedStock;
    /** Fecha del último restock */
    private LocalDateTime lastRestockDate;
    private LocalDateTime updatedAt;
}
