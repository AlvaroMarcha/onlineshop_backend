package es.marcha.backend.modules.catalog.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProductImageReorderItemDTO {
    /** ID de la imagen a reordenar. */
    private Long id;
    /** Nuevo valor de {@code sortOrder} (1-based). */
    private int sortOrder;
}
