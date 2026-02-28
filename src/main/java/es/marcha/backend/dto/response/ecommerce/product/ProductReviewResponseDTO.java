package es.marcha.backend.dto.response.ecommerce.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta utilizado para exponer información de reseñas de productos
 * en el módulo de e-commerce.
 *
 * <p>
 * Este objeto se utiliza exclusivamente para la capa de presentación,
 * evitando exponer directamente la entidad de base de datos.
 * </p>
 *
 * <p>
 * Incluye información básica de la reseña, datos del producto asociado
 * y del usuario que la realizó, así como métricas de interacción.
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductReviewResponseDTO {
    private long id;
    private long productId;
    private ProductReviewUserResponseDTO user;
    private int rating;
    private String title;
    private String comment;
    private int likes;
    private int dislikes;
    private boolean isActive;

}
