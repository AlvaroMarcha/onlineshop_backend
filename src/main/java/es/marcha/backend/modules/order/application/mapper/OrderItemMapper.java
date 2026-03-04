package es.marcha.backend.modules.order.application.mapper;

import es.marcha.backend.modules.order.application.dto.response.OrderItemsResponseDTO;
import es.marcha.backend.modules.order.domain.model.OrderItems;

public class OrderItemMapper {
    public static OrderItemsResponseDTO toOrderItemDTO(OrderItems orderItems) {
        return OrderItemsResponseDTO.builder()
                .id(orderItems.getId())
                .productId(orderItems.getProduct() != null ? orderItems.getProduct().getId() : 0)
                .name(orderItems.getName())
                .description(orderItems.getDescription())
                .sku(orderItems.getSku())
                .price(orderItems.getPrice())
                .discountPrice(orderItems.getDiscountPrice())
                .quantity(orderItems.getQuantity())
                .taxRate(orderItems.getTaxRate())
                .weight(orderItems.getWeight())
                .isDigital(orderItems.isDigital())
                .isFeatured(orderItems.isFeatured())
                .build();
    }
}
