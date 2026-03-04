package es.marcha.backend.modules.order.application.dto.request;

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
