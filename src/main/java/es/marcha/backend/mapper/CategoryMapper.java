package es.marcha.backend.mapper;

import es.marcha.backend.model.ecommerce.Category;

import es.marcha.backend.dto.response.CategoryResponseDTO;

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
