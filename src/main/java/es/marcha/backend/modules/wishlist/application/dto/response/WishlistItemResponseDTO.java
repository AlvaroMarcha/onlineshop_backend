package es.marcha.backend.modules.wishlist.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class WishlistItemResponseDTO {

    private long id;
    private long productId;
    private String productName;
    private String productSlug;
    private BigDecimal productPrice;
    private BigDecimal productDiscountPrice;
    private String productMainImageUrl;
    private boolean productIsActive;
    private int productStock;
    private LocalDateTime addedAt;
}
