package es.marcha.backend.modules.catalog.infrastructure.persistence;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.enums.MovementType;
import es.marcha.backend.modules.catalog.domain.model.Movement;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    /**
     * Obtiene todos los movimientos de un producto, ordenados por fecha
     * descendente.
     *
     * @param productId ID del producto
     * @param pageable  configuración de paginación
     * @return página de movimientos
     */
    Page<Movement> findAllByProductIdOrderByCreatedAtDesc(long productId, Pageable pageable);

    /**
     * Obtiene todos los movimientos de un tipo concreto para un producto.
     *
     * @param productId    ID del producto
     * @param movementType tipo de movimiento
     * @return lista de movimientos
     */
    List<Movement> findAllByProductIdAndMovementType(long productId, MovementType movementType);
}
