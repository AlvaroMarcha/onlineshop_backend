package es.marcha.backend.dto.request.ecommerce.product;

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
public class ProductAttribValueRequestDTO {
    private long attribId;
    private String value;
    private String label;
    private String colorHex;
    private int sortOrder;
    private boolean isActive;
}
