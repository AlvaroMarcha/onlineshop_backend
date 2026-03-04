package es.marcha.backend.modules.catalog.application.dto.response.product;

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
public class ProductImageResponseDTO {
    private Long id;
    private String url;
    private String altText;
    private int sortOrder;
    private boolean isMain;
    private LocalDateTime uploadedAt;
}
