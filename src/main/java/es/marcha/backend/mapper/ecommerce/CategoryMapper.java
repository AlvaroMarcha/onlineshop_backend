package es.marcha.backend.mapper.ecommerce;

import es.marcha.backend.dto.response.ecommerce.CategoryResponseDTO;
import es.marcha.backend.model.ecommerce.Category;

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
