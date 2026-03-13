package es.marcha.backend.modules.dashboard.application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para reseñas pendientes de moderación.
 * <p>
 * Representa una reseña que requiere aprobación del equipo de ADMIN/STORE.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReviewDTO {

    /**
     * ID de la reseña.
     */
    private long reviewId;

    /**
     * ID del producto asociado.
     */
    private long productId;

    /**
     * Nombre del producto.
     */
    private String productName;

    /**
     * Calificación (1-5 estrellas).
     */
    private int rating;

    /**
     * Comentario de la reseña (puede ser null).
     */
    private String comment;

    /**
     * ID del usuario que creó la reseña.
     */
    private long userId;

    /**
     * Nombre del usuario.
     */
    private String userName;

    /**
     * Fecha de creación de la reseña.
     */
    private LocalDateTime createdAt;

    /**
     * Indica si la reseña está aprobada.
     */
    private boolean isApproved;
}
