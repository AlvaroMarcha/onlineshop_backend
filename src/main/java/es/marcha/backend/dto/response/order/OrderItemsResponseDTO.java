package es.marcha.backend.dto.response.order;

import java.math.BigDecimal;

import es.marcha.backend.model.ecommerce.Product;
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
    // Attribs
    private long id;
    private Product product;
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
