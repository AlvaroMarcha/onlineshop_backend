package es.marcha.backend.mapper.ecommerce;

import es.marcha.backend.dto.response.ecommerce.product.ProductReviewResponseDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductReviewResponseProductDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductReviewUserResponseDTO;
import es.marcha.backend.model.ecommerce.product.ProductReview;
import es.marcha.backend.model.user.User;

public class ProductReviewMapper {

    public static ProductReviewResponseDTO toProductReviewDTO(ProductReview productReview) {
        return ProductReviewResponseDTO.builder()
                .id(productReview.getId())
                .productId(productReview.getProduct().getId())
                .user(toReviewUser(productReview.getUser()))
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
                // !! Esto no funciona, pero no se esta usando revisar la proxima vez !!
                // .user(toReviewUser(productReview.getUser()))
                .rating(productReview.getRating())
                .title(productReview.getTitle())
                .comment(productReview.getComment())
                .likes(productReview.getLikes())
                .dislikes(productReview.getDislikes())
                .isActive(productReview.isActive())
                .build();
    }

    public static ProductReviewUserResponseDTO toReviewUser(User user) {
        return ProductReviewUserResponseDTO.builder()
                .userId(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }
}
