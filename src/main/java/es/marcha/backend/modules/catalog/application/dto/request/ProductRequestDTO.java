package es.marcha.backend.modules.catalog.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal taxRate;
    private List<Long> subcategoryIds;
    /** Usuario que crea el producto (nombre o email del admin) */
    private String createdBy;
    private boolean isDigital;
    private double weight;
    private boolean isFeatured;
    /** Stock inicial obligatorio: debe ser >= 1 al crear el producto */
    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 1, message = "El stock inicial debe ser mayor que 0")
    private Integer stock;

}
