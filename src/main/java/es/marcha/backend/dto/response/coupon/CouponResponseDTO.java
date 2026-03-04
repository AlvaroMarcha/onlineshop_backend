package es.marcha.backend.dto.response.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import es.marcha.backend.core.shared.domain.enums.DiscountType;
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
public class CouponResponseDTO {

    private long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private Integer maxUses;
    private Integer maxUsesPerUser;
    private int usedCount;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private boolean isActive;
    /** IDs de usuarios a los que aplica. Lista vacía = aplica a todos. */
    private List<Long> applicableUserIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
