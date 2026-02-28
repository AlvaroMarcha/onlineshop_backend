package es.marcha.backend.mapper.product;

import es.marcha.backend.dto.response.ecommerce.SubcategoryResponseDTO;
import es.marcha.backend.model.ecommerce.Subcategory;

public class SubcategoryMapper {
    public static SubcategoryResponseDTO toResponseDTO(Subcategory subcategory) {
        return SubcategoryResponseDTO.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .description(subcategory.getDescription())
                .slug(subcategory.getSlug())
                .build();
    }

    public static Subcategory toSubcategory(SubcategoryResponseDTO dto, Subcategory subcategory) {
        subcategory.setName(dto.getName());
        subcategory.setDescription(dto.getDescription());
        subcategory.setSlug(dto.getSlug());
        return subcategory;
    }

}
