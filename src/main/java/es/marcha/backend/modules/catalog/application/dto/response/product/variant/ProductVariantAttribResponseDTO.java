package es.marcha.backend.modules.catalog.application.dto.response.product.variant;

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
public class ProductVariantAttribResponseDTO {
    private long id;
    private long variantId;
    private long attribId;
    private String attribName;
    private long attribValueId;
    private String value;
    private String label;
    private String colorHex;
}
