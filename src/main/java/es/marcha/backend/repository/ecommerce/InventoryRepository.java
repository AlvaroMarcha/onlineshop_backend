package es.marcha.backend.repository.ecommerce;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
