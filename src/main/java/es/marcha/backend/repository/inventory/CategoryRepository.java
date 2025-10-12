package es.marcha.backend.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.marcha.backend.model.inventory.Category; // <--- ESTE IMPORT

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {}