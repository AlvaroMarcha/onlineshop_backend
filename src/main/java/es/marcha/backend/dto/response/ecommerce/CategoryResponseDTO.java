package es.marcha.backend.dto.response.ecommerce;

import java.util.List;

import es.marcha.backend.model.ecommerce.Subcategory;
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
