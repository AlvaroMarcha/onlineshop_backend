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
    /**
     * ID de la dirección de envío elegida por el usuario.
     * Si es null, se usará la dirección marcada como predeterminada.
     */
    private Long addressId;
    private List<OrderItemRequestDTO> items;
}
