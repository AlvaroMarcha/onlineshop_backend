package es.marcha.backend.mapper.ecommerce;

import java.util.Collections;
import java.util.List;

import es.marcha.backend.dto.request.ecommerce.ProductRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductResponseDTO;
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.model.ecommerce.product.Product;

public class ProductMapper {

    public static ProductResponseDTO toProductDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .categories(product.getCategories())
                .description(product.getDescription())
                .reviews(product.getReviews().stream().map(ProductReviewMapper::toProductReviewDTO).toList())
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
                .attribs(product.getAttribs() != null
                        ? ProductAttribMapper.toResponseDTOList(product.getAttribs())
                        : Collections.emptyList())
                .variants(Collections.emptyList())
                .build();
    }

    /**
     * Convierte un producto a DTO incluyendo la lista completa de variantes.
     * Usar exclusivamente en el endpoint de detalle de producto.
     *
     * @param product entidad a convertir
     * @return DTO con todos los campos incluidas las variantes
     */
    public static ProductResponseDTO toProductDetailDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .categories(product.getCategories())
                .description(product.getDescription())
                .reviews(product.getReviews().stream().map(ProductReviewMapper::toProductReviewDTO).toList())
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
                .attribs(product.getAttribs() != null
                        ? ProductAttribMapper.toResponseDTOList(product.getAttribs())
                        : Collections.emptyList())
                .variants(product.getVariants() != null
                        ? ProductVariantMapper.toResponseDTOList(product.getVariants())
                        : Collections.emptyList())
                .build();
    }

    public static Product toProductByRequestProduct(ProductRequestDTO productDTO, List<Subcategory> subcategories) {
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
                .subcategories(subcategories)
                .build();
    }
}
