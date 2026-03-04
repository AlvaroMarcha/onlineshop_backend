package es.marcha.backend.modules.catalog.application.mapper;

import es.marcha.backend.modules.catalog.application.dto.response.SubcategoryResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.Subcategory;

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
