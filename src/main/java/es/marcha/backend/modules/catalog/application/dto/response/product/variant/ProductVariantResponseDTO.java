package es.marcha.backend.modules.catalog.application.dto.response.product.variant;

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
public class ProductVariantResponseDTO {
    private long id;
    private long productId;
    private String sku;
    private BigDecimal priceOverride;
    private BigDecimal discountPriceOverride;
    private int stock;
    private boolean isDefault;
    private boolean isActive;
    private List<ProductVariantAttribResponseDTO> attribs;
    private LocalDateTime createdAt;
}
