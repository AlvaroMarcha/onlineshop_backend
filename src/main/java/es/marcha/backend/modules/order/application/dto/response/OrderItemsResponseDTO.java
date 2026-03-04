package es.marcha.backend.modules.order.application.dto.response;

import java.math.BigDecimal;

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
public class OrderItemsResponseDTO {
    private long id;
    private long productId;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private int quantity;
    private BigDecimal taxRate;
    private double weight;
    private boolean isDigital;
    private boolean isFeatured;
}
