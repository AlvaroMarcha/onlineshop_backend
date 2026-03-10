package es.marcha.backend.modules.catalog.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.model.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Obtiene el inventario asociado a un producto por su ID.
     *
     * @param productId ID del producto
     * @return Optional con el inventario, o vacío si no existe
     */
    Optional<Inventory> findByProductId(long productId);
}
