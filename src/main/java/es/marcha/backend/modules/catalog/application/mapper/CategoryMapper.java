package es.marcha.backend.modules.catalog.application.mapper;

import es.marcha.backend.modules.catalog.application.dto.response.CategoryResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.Category;

public class CategoryMapper {
    public static CategoryResponseDTO toCategoryDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .subcategories(category.getSubcategories())
                .build();
    }
}
