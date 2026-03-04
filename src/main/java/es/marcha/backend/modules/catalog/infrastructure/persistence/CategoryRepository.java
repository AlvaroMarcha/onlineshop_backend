package es.marcha.backend.modules.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
