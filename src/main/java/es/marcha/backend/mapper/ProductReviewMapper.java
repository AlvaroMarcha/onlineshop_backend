package es.marcha.backend.mapper;

import es.marcha.backend.dto.response.ecommerce.ProductReviewResponseDTO;
import es.marcha.backend.dto.response.ecommerce.ProductReviewResponseProductDTO;
import es.marcha.backend.model.ecommerce.ProductReview;

public class ProductReviewMapper {
    public static ProductReviewResponseDTO toProductReviewDTO(ProductReview productReview) {
        return ProductReviewResponseDTO.builder()
                .id(productReview.getId())
                .productId(productReview.getProduct().getId())
                .user(UserMapper.toUserDTO(productReview.getUser()))
                .rating(productReview.getRating())
                .title(productReview.getTitle())
                .comment(productReview.getComment())
                .likes(productReview.getLikes())
                .dislikes(productReview.getDislikes())
                .isActive(productReview.isActive())
                .build();
    }

    public static ProductReviewResponseProductDTO toProductReviewProductDTO(ProductReview productReview) {
        return ProductReviewResponseProductDTO.builder()
                .id(productReview.getId())
                .user(UserMapper.toUserDTO(productReview.getUser()))
                .rating(productReview.getRating())
                .title(productReview.getTitle())
                .comment(productReview.getComment())
                .likes(productReview.getLikes())
                .dislikes(productReview.getDislikes())
                .isActive(productReview.isActive())
                .build();
    }
}
