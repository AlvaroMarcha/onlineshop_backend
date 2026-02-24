package es.marcha.backend.dto.request.ecommerce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
public class ProductRequestDTO {
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal taxRate;
    private List<Long> subcategoryIds;
    private String createdBy;
    private LocalDateTime createdAt;
    private boolean isDigital;
    private double weight;
    private boolean isActive;
    private boolean isFeatured;

}
