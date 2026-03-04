package es.marcha.backend.modules.catalog.application.dto.response.product;

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
public class ProductReviewUserResponseDTO {
    // Attribs
    private long userId;
    private String name;
    private String surname;
}
