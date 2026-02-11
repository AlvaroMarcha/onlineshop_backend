package es.marcha.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import es.marcha.backend.model.ecommerce.Subcategory;
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
    private List<Subcategory> subcategories;
    private boolean isActive;
    private boolean isDeleted;
    private String createdBy;
    private LocalDateTime createdAt;
    private double weight;
    private boolean isDigital;
    private boolean isFeatured;
    // SEO && Marketing
    private String slug;
    private String metaTitle;
    private String metaDescription;
    private double rating;
    private double ratingCount;

}
