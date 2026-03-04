package es.marcha.backend.modules.catalog.application.dto.request.product;

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
    private String label;
    private String colorHex;
    private int sortOrder;
    private boolean isActive;
}
