package es.marcha.backend.modules.catalog.application.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.catalog.application.dto.response.product.ProductReviewResponseDTO;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.application.mapper.ProductReviewMapper;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.domain.model.product.ProductReview;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductReviewRepository;
import es.marcha.backend.core.user.application.service.UserService;
import es.marcha.backend.core.shared.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class ProductReviewService {

    @Autowired
    private ProductReviewRepository pReviewRepository;

    @Autowired
    private ProductRepository prodRepository;

    @Autowired
    private UserService uService;

    public static final String REVIEW_DELETED = "REVIEW WAS DELETED";

    /**
     * Obtiene todas las reseñas activas y no eliminadas de un producto.
     * Lanza excepción si no hay ninguna reseña.
     *
     * @param productId El ID del producto cuyas reseñas se desean obtener.
     * @return Lista de {@link ProductReviewResponseDTO} con las reseñas del
     *         producto.
     * @throws ProductException si el producto no tiene reseñas activas.
     */
    public List<ProductReviewResponseDTO> getAllReviewsByProduct(long productId) {
        List<ProductReviewResponseDTO> reviews = pReviewRepository.findAllByProductId(productId).stream()
                .filter(review -> review.isActive() && !review.isDeleted())
                .map(ProductReviewMapper::toProductReviewDTO)
                .toList();

        if (reviews == null || reviews.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_REVIEWS);
        }

        return reviews;
    }

    /**
     * Obtiene todas las reseñas activas y no eliminadas de un producto para uso
     * interno.
     * A diferencia de {@link #getAllReviewsByProduct}, no lanza excepción si la
     * lista está vacía.
     *
     * @param productId El ID del producto cuyas reseñas se desean obtener.
     * @return Lista de {@link ProductReviewResponseDTO}, posiblemente vacía.
     */
    public List<ProductReviewResponseDTO> getAllReviewsByProductHandler(long productId) {
        List<ProductReviewResponseDTO> reviews = pReviewRepository.findAllByProductId(productId).stream()
                .filter(review -> review.isActive() && !review.isDeleted())
                .map(ProductReviewMapper::toProductReviewDTO)
                .toList();

        return reviews;
    }

    @Transactional
    public ProductReviewResponseDTO addNewReview(ProductReview review) {
        if (review == null)
            throw new ProductException(ProductException.FAILED_CREATE_REVIEW);

        review.setLikes(0);
        review.setDislikes(0);
        review.setActive(true);
        review.setDeleted(false);
        review.setCreatedAt(LocalDateTime.now());

        User user = uService.getUserByIdForHandler(review.getUser().getId());

        ProductUtils.validateRating(review.getRating());

        review.setUser(user);

        return ProductReviewMapper.toProductReviewDTO(pReviewRepository.save(review));
    }

    /**
     * Persiste una reseña directamente en la base de datos para uso interno.
     * No inicializa campos por defecto; debe usarse cuando la entidad ya está
     * preparada.
     *
     * @param review La reseña {@link ProductReview} a guardar.
     * @return La entidad {@link ProductReview} persistida.
     * @throws ProductException si la reseña es {@code null}.
     */
    public ProductReview saveReview(ProductReview review) {
        if (review == null)
            throw new ProductException(ProductException.FAILED_SAVE_REVIEW);

        return pReviewRepository.save(review);
    }

    @Transactional
    public ProductReviewResponseDTO updateReview(ProductReview review) {
        if (review == null)
            throw new ProductException(ProductException.FAILED_UPDATE_REVIEW);

        ProductReview updatedReview = pReviewRepository.findById(review.getId())
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_REVIEW));

        // Guardar el rating antiguo para recalcular el rating del producto si cambia
        int oldRating = updatedReview.getRating();
        boolean ratingChanged = false;

        // Actualizar rating si se proporciona y es diferente
        if (review.getRating() != oldRating) {
            ProductUtils.validateRating(review.getRating());
            updatedReview.setRating(review.getRating());
            ratingChanged = true;
        }

        // Actualizar título y comentario
        updatedReview.setTitle(review.getTitle());
        updatedReview.setComment(review.getComment());
        updatedReview.setUpdatedAt(LocalDateTime.now());

        // Si cambió el rating, recalcular el rating del producto
        if (ratingChanged) {
            Product product = updatedReview.getProduct();

            // Ajustar el rating del producto: restar el antiguo y sumar el nuevo
            double currentTotalRating = product.getRating() != null ? product.getRating() : 0.0;
            double newTotalRating = currentTotalRating - oldRating + review.getRating();

            product.setRating(newTotalRating);
            prodRepository.save(product);
        }

        return ProductReviewMapper.toProductReviewDTO(saveReview(updatedReview));
    }

    /**
     * Realiza la eliminación lógica de una reseña por su ID.
     * Marca la reseña como inactiva y eliminada sin borrarla físicamente.
     *
     * @param id El ID de la reseña a eliminar.
     * @return Mensaje de confirmación de la eliminación.
     * @throws ProductException si la reseña no existe.
     */
    @Transactional
    public String deleteReview(long id) {
        ProductReview deletedReview = pReviewRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_REVIEW));

        deletedReview.setActive(false);
        deletedReview.setDeleted(true);
        deletedReview.setUpdatedAt(LocalDateTime.now());
        saveReview(deletedReview);

        return REVIEW_DELETED;
    }

    /**
     * Incrementa en uno el contador de likes de una reseña.
     *
     * @param id El ID de la reseña a la que se incrementan los likes.
     * @return Mapa con {@code reviewId} y {@code likes} actualizados.
     * @throws ProductException si la reseña no existe.
     */
    @Transactional
    public Map<String, Integer> incrementLikes(long id) {
        Map<String, Integer> likes = new HashMap<>();
        ProductReview reviewLikes = pReviewRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_REVIEW));

        reviewLikes.setLikes(reviewLikes.getLikes() + 1);
        likes.put("reviewId", (int) reviewLikes.getId());
        likes.put("likes", reviewLikes.getLikes());
        saveReview(reviewLikes);

        return likes;
    }

    /**
     * Incrementa en uno el contador de dislikes de una reseña.
     *
     * @param id El ID de la reseña a la que se incrementan los dislikes.
     * @return Mapa con {@code reviewId} y {@code dislikes} actualizados.
     * @throws ProductException si la reseña no existe.
     */
    @Transactional
    public Map<String, Integer> incrementDislikes(long id) {
        Map<String, Integer> dislikes = new HashMap<>();
        ProductReview reviewDislikes = pReviewRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_REVIEW));

        reviewDislikes.setLikes(reviewDislikes.getLikes() + 1);
        dislikes.put("reviewId", (int) reviewDislikes.getId());
        dislikes.put("dislikes", reviewDislikes.getLikes());
        saveReview(reviewDislikes);

        return dislikes;
    }
}
