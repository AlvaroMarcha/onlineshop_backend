package es.marcha.backend.dto.request.order;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OrderRequestDTO {
    private long userId;
    private List<OrderItemRequestDTO> items;
}
