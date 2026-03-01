package es.marcha.backend.mapper.order;

import es.marcha.backend.dto.response.order.OrderItemsResponseDTO;
import es.marcha.backend.model.order.OrderItems;

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
