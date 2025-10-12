package es.marcha.backend.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.marcha.backend.model.inventory.Subcategory; // <--- ESTE IMPORT

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {}