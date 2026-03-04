package es.marcha.backend.modules.catalog.application.dto.response;

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
public class SubcategoryResponseDTO {
    private long id;
    private String name;
    private String description;
    private String slug;

}
