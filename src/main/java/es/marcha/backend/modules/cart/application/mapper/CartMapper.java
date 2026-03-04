package es.marcha.backend.modules.cart.application.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import es.marcha.backend.modules.cart.application.dto.response.CartItemResponseDTO;
import es.marcha.backend.modules.cart.application.dto.response.CartResponseDTO;
import es.marcha.backend.modules.cart.domain.model.Cart;
import es.marcha.backend.modules.cart.domain.model.CartItem;

public class CartMapper {

    private CartMapper() {
    }

    public static CartItemResponseDTO toItemDTO(CartItem item) {
        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantSku(item.getVariant() != null ? item.getVariant().getSku() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static CartResponseDTO toCartDTO(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems() == null
                ? Collections.emptyList()
                : cart.getItems().stream().map(CartMapper::toItemDTO).toList();

        BigDecimal total = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .status(cart.getStatus())
                .items(itemDTOs)
                .total(total)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .expiresAt(cart.getExpiresAt())
                .build();
    }
}
