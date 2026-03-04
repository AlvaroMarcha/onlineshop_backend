package es.marcha.backend.dto.response.order;

import java.time.LocalDateTime;
import java.util.List;

import es.marcha.backend.core.shared.domain.enums.OrderStatus;
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
public class OrderResponseDTO {
    private long id;
    private long userId;
    private OrderStatus status;
    private double totalAmount;
    private double discountAmount;
    private String couponCode;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<PaymentResponseDTO> payments;
    private OrderAddrResponseDTO address;
    private List<OrderItemsResponseDTO> orderItems;
}
