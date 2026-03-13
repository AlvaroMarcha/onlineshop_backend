package es.marcha.backend.modules.catalog.application.dto.request.product;

import java.math.BigDecimal;
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
public class ProductVariantRequestDTO {
    private BigDecimal priceOverride;
    private BigDecimal discountPriceOverride;
    private int stock;
    private boolean isDefault;
    private boolean isActive;
    private List<Long> attribValueIds;
}
