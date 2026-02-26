package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.ecommerce.ProductReviewResponseDTO;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.mapper.ProductReviewMapper;
import es.marcha.backend.model.ecommerce.ProductReview;
import es.marcha.backend.repository.ecommerce.ProductReviewRepository;
import jakarta.transaction.Transactional;

@Service
public class ProductReviewService {

    @Autowired
    private ProductReviewRepository pReviewRepository;

    public static final String REVIEW_DELETED = "REVIEW WAS DELETED";

    public List<ProductReviewResponseDTO> getAllReviewsByProduct(long productId) {
        List<ProductReviewResponseDTO> reviews = pReviewRepository.findAllByProductId(productId).stream()
                .filter(ProductReview::isActive)
                .map(ProductReviewMapper::toProductReviewDTO)
                .toList();

        if (reviews == null || reviews.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_REVIEWS);
        }

        return reviews;
    }

    @Transactional
    public ProductReviewResponseDTO addNewReview(ProductReview review) {
        if (review == null)
            throw new ProductException(ProductException.FAILED_CREATE_REVIEW);

        review.setLikes(0);
        review.setDislikes(0);
        review.setActive(true);
        review.setCreatedAt(LocalDateTime.now());

        return ProductReviewMapper.toProductReviewDTO(pReviewRepository.save(review));
    }

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

        updatedReview.setRating(review.getRating());
        updatedReview.setTitle(review.getTitle());
        updatedReview.setComment(review.getComment());
        updatedReview.setUpdatedAt(LocalDateTime.now());

        return ProductReviewMapper.toProductReviewDTO(saveReview(updatedReview));
    }

    @Transactional
    public String deleteReview(long id) {
        ProductReview deletedReview = pReviewRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_REVIEW));

        deletedReview.setActive(false);
        deletedReview.setUpdatedAt(LocalDateTime.now());
        saveReview(deletedReview);

        return REVIEW_DELETED;
    }

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
