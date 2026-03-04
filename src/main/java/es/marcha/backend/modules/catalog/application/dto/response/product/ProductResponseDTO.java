package es.marcha.backend.modules.catalog.application.dto.response.product;

import java.math.BigDecimal;
import java.util.List;

import es.marcha.backend.modules.catalog.application.dto.response.product.attrib.ProductAttribResponseDTO;
import es.marcha.backend.modules.catalog.application.dto.response.product.variant.ProductVariantResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductResponseDTO {
    private long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal taxRate;
    private List<Category> categories;
    private double weight;
    private boolean isDigital;
    private boolean isFeatured;
    private String slug;
    private String metaTitle;
    private String metaDescription;
    private double rating;
    private double ratingCount;
    private int soldCount;
    private int stock;
    private Integer lowStockThreshold;
    private boolean isActive;
    private List<ProductReviewResponseDTO> reviews;
    private List<ProductAttribResponseDTO> attribs;
    private List<ProductVariantResponseDTO> variants;
    private String mainImageUrl;
    private List<ProductImageResponseDTO> images;
    // Solo se rellena cuando hay un usuario autenticado; null en caso contrario
    private Boolean isInWishlist;
}
