package es.marcha.backend.modules.catalog.application.dto.response;

import java.util.List;

import es.marcha.backend.modules.catalog.domain.model.Subcategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class CategoryResponseDTO {
    private long id;
    private String name;
    private String description;
    private String slug;
    private List<Subcategory> subcategories;
}
