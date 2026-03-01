package es.marcha.backend.dto.request.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OrderItemRequestDTO {
    private long productId;
    private int quantity;
}
