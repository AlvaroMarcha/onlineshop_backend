package es.marcha.backend.dto.response.ecommerce.product;

import es.marcha.backend.core.user.application.dto.response.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta utilizado para representar reseñas asociadas a un producto
 * cuando se consulta el detalle del mismo.
 *
 * <p>
 * A diferencia de {@link ProductReviewResponseDTO}, este DTO no incluye
 * el identificador del producto, ya que se asume que la reseña se encuentra
 * dentro del contexto de un producto ya cargado.
 * </p>
 *
 * <p>
 * Su finalidad es optimizar la respuesta del endpoint de producto,
 * incluyendo únicamente la información necesaria para mostrar reseñas.
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductReviewResponseProductDTO {
    private long id;
    private UserResponseDTO user;
    private int rating;
    private String title;
    private String comment;
    private int likes;
    private int dislikes;
    private boolean isActive;

}
