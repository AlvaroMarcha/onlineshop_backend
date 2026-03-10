package es.marcha.backend.modules.catalog.application.mapper;

import es.marcha.backend.modules.catalog.application.dto.response.inventory.MovementResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.Movement;

public class MovementMapper {

    private MovementMapper() {
    }

    /**
     * Convierte una entidad {@link Movement} a su DTO de respuesta.
     *
     * @param movement entidad a convertir
     * @return {@link MovementResponseDTO} con los datos del movimiento
     */
    public static MovementResponseDTO toMovementDTO(Movement movement) {
        return MovementResponseDTO.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .productSku(movement.getProduct().getSku())
                .quantity(movement.getQuantity())
                .previousQuantity(movement.getPreviousQuantity())
                .newQuantity(movement.getNewQuantity())
                .movementType(movement.getMovementType())
                .notes(movement.getNotes())
                .createdBy(movement.getCreatedBy())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
