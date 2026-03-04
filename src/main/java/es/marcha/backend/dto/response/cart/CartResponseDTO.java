package es.marcha.backend.dto.response.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import es.marcha.backend.core.shared.domain.enums.CartStatus;
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
public class CartResponseDTO {

    private long id;
    private long userId;
    private CartStatus status;
    private List<CartItemResponseDTO> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
