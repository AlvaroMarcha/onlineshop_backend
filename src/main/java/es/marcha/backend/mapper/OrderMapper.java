package es.marcha.backend.mapper;

import es.marcha.backend.dto.response.order.OrderResponseDTO;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.user.User;

public class OrderMapper {

        public static OrderResponseDTO toOrderDTO(Order order) {
                return OrderResponseDTO.builder()
                                .id(order.getId())
                                .userId(order.getUser().getId())
                                .status(order.getStatus())
                                .totalAmount(order.getTotalAmount())
                                .paymentMethod(order.getPaymentMethod())
                                .createdAt(order.getCreatedAt())
                                .payments(order.getPayments() != null
                                                ? order.getPayments().stream()
                                                                .map(PaymentMapper::toPaymentDTO)
                                                                .toList()
                                                : null)
                                .build();

        }

        public static Order toOrder(OrderResponseDTO dto, User user) {
                Order order = Order.builder()
                                .id(dto.getId())
                                .user(user)
                                .status(dto.getStatus())
                                .totalAmount(dto.getTotalAmount())
                                .paymentMethod(dto.getPaymentMethod())
                                .createdAt(dto.getCreatedAt())
                                .build();

                if (dto.getPayments() != null) {
                        order.setPayments(dto.getPayments().stream()
                                        .map(paymentDto -> PaymentMapper.toPayment(paymentDto, order))
                                        .toList());
                }

                return order;
        }

}
