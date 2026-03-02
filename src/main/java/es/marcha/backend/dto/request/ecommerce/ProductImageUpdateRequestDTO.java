package es.marcha.backend.dto.request.ecommerce;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProductImageUpdateRequestDTO {
    private String altText;
    private Boolean isMain;
    private Integer sortOrder;
}
