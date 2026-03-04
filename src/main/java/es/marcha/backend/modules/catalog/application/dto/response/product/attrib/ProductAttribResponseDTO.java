package es.marcha.backend.modules.catalog.application.dto.response.product.attrib;

import java.time.LocalDateTime;
import java.util.List;

import es.marcha.backend.core.shared.domain.enums.AttribType;
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
public class ProductAttribResponseDTO {
    private long id;
    private String name;
    private String description;
    private String slug;
    private AttribType type;
    private boolean isRequired;
    private int sortOrder;
    private List<ProductAttribValueResponseDTO> values;
    private LocalDateTime createdAt;
}
