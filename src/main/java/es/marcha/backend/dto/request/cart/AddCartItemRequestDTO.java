package es.marcha.backend.dto.request.cart;

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
public class AddCartItemRequestDTO {

    private long productId;
    private Long variantId;
    private int quantity;
}
