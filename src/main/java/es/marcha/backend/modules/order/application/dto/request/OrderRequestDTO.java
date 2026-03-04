package es.marcha.backend.modules.order.application.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OrderRequestDTO {
    private long userId;
    private Long addressId;
    private List<OrderItemRequestDTO> items;
    private String couponCode;
}
