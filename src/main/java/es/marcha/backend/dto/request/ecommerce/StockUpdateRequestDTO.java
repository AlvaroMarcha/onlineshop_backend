package es.marcha.backend.dto.request.ecommerce;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para actualizar el stock de un producto manualmente desde el dashboard de admin.
 * Se usa en PATCH /products/{id}/stock
 */
@NoArgsConstructor
@Getter
@Setter
public class StockUpdateRequestDTO {

    /** Nuevo valor de stock. Debe ser >= 0 */
    private int stock;
}
