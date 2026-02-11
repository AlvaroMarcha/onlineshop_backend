package es.marcha.backend.mapper;

import es.marcha.backend.dto.response.ProductReponseDTO;
import es.marcha.backend.model.ecommerce.Product;

public class ProductMapper {
    public static ProductReponseDTO toProductDTO(Product product) {
        return ProductReponseDTO.builder()
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
