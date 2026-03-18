package es.marcha.backend.modules.catalog.infrastructure.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.catalog.domain.model.Subcategory;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Subcategory s SET s.isActive = :active, s.updatedAt = :updatedAt WHERE s.category.id = :categoryId")
    void updateActiveByCategoryId(
            @Param("categoryId") Long categoryId,
            @Param("active") boolean active,
            @Param("updatedAt") LocalDateTime updatedAt);
}
