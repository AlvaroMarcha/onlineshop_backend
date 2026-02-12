package es.marcha.backend.repository.ecommerce;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.ecommerce.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
