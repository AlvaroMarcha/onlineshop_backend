package es.marcha.backend.modules.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.model.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
