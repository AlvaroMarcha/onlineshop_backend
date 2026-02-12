package es.marcha.backend.mapper;

import es.marcha.backend.dto.request.ProductRequestDTO;
import es.marcha.backend.dto.response.ProductResponseDTO;
import es.marcha.backend.model.ecommerce.Product;

public class ProductMapper {
    public static ProductResponseDTO toProductDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .subcategories(product.getSubcategories())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .taxRate(product.getTaxRate())
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

    public static Product toProductByRequestProduct(ProductRequestDTO productDTO) {
        return Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .discountPrice(productDTO.getDiscountPrice())
                .taxRate(productDTO.getTaxRate())
                .weight(productDTO.getWeight())
                .createdBy(productDTO.getCreatedBy())
                .isDigital(productDTO.isDigital())
                .isFeatured(productDTO.isFeatured())
                .build();
    }
}
