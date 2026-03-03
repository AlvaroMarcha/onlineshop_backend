package es.marcha.backend.dto.request.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.marcha.backend.model.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CouponRequestDTO {

    @NotBlank(message = "El código del cupón es obligatorio")
    private String code;
    private String description;
    @NotNull(message = "El tipo de descuento es obligatorio")
    private DiscountType discountType;
    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor del descuento debe ser mayor que 0")
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private Integer maxUses;
    /** Límite de usos por usuario (null = sin límite individual). */
    private Integer maxUsesPerUser;
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate validFrom;
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate validUntil;
    @Builder.Default
    private boolean isActive = true;
    /**
     * IDs de usuario a los que se restringe este cupón.
     * Lista vacía significa que aplica a todos los usuarios.
     */
    @Builder.Default
    private List<Long> applicableUserIds = new ArrayList<>();
}
