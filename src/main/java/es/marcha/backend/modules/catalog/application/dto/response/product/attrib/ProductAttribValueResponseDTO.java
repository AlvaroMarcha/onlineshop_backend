package es.marcha.backend.modules.catalog.application.dto.response.product.attrib;

import java.time.LocalDateTime;

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
public class ProductAttribValueResponseDTO {
    private long id;
    private long attribId;
    private String value;
    private String label;
    private String colorHex;
    private int sortOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
}
