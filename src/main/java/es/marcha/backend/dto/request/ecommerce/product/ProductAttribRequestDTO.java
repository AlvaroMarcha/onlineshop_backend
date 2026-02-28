package es.marcha.backend.dto.request.ecommerce.product;

import es.marcha.backend.model.enums.AttribType;
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
public class ProductAttribRequestDTO {
    private String name;
    private String description;
    private AttribType type;
    private boolean isRequired;
    private int sortOrder;
}
