package es.marcha.backend.modules.dashboard.application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para reseñas recientes (rol STORE).
 * <p>
 * Representa una reseña reciente de un producto.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentReviewDTO {

    /**
     * ID de la reseña.
     */
    private long reviewId;

    /**
     * ID del producto.
     */
    private long productId;

    /**
     * Nombre del producto.
     */
    private String productName;

    /**
     * Valoración (1-5).
     */
    private int rating;

    /**
     * Comentario de la reseña.
     */
    private String comment;

    /**
     * Fecha de creación.
     */
    private LocalDateTime createdAt;

    /**
     * Nombre del usuario que dejó la reseña.
     */
    private String userName;

    /**
     * URL de la imagen del producto.
     */
    private String productImageUrl;
}
