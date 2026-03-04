package es.marcha.backend.modules.order.application.mapper;

import es.marcha.backend.modules.order.application.dto.response.OrderAddrResponseDTO;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.domain.model.OrderAddresses;
import es.marcha.backend.core.user.domain.model.Address;

public class OrderAddrMapper {
    public static OrderAddrResponseDTO toOrderAddressDTO(OrderAddresses orderAddress) {
        return OrderAddrResponseDTO.builder()
                .id(orderAddress.getId())
                .type(orderAddress.getType())
                .addressLine1(orderAddress.getAddressLine1())
                .addressLine2(orderAddress.getAddressLine2())
                .city(orderAddress.getCity())
                .postalCode(orderAddress.getPostalCode())
                .country(orderAddress.getCountry())
                .isDefault(orderAddress.isDefault())
                .createdAt(orderAddress.getCreatedAt())
                .build();
    }

    public static OrderAddresses fromAddresstoOrderAddr(Address address, Order order) {
        return OrderAddresses.builder()
                .order(order)
                .type(address.getType())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .build();

    }
}
