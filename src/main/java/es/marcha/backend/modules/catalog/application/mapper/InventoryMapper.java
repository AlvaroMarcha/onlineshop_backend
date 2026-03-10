package es.marcha.backend.modules.catalog.application.mapper;

import es.marcha.backend.modules.catalog.application.dto.response.inventory.InventoryResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.Inventory;

public class InventoryMapper {

    private InventoryMapper() {
    }

    /**
     * Convierte una entidad {@link Inventory} a su DTO de respuesta.
     *
     * @param inventory entidad a convertir
     * @return {@link InventoryResponseDTO} con los datos del inventario
     */
    public static InventoryResponseDTO toInventoryDTO(Inventory inventory) {
        return InventoryResponseDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .minStock(inventory.getMinStock())
                .maxStock(inventory.getMaxStock())
                .incomingStock(inventory.getIncomingStock())
                .damagedStock(inventory.getDamagedStock())
                .lastRestockDate(inventory.getLastRestockDate())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
