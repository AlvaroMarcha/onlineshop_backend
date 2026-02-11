package es.marcha.backend.mapper;

import es.marcha.backend.dto.response.ProductResponseDTO;
import es.marcha.backend.model.ecommerce.Product;

public class ProductMapper {
    public static ProductResponseDTO toProductDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .taxRate(product.getTaxRate())
                .isActive(product.isActive())
                .isDeleted(product.isDeleted())
                .createdBy(product.getCreatedBy())
                .weight(product.getWeight())
                .isDigital(product.isDigital())
                .isFeatured(product.isFeatured())
                .slug(product.getSlug())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .rating(product.getRating())
                .ratingCount(product.getRatingCount())
                .build();
    }
}
