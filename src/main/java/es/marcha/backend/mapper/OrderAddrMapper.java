package es.marcha.backend.mapper;

import es.marcha.backend.dto.response.OrderAddrResponseDTO;
import es.marcha.backend.model.order.OrderAddresses;

public class OrderAddrMapper {
    public static OrderAddrResponseDTO toOrderAddressDTO(OrderAddresses orderAddress) {
        return OrderAddrResponseDTO.builder()
                .id(orderAddress.getId())
                .order(orderAddress.getOrder())
                .type(orderAddress.getType())
                .addressLine1(orderAddress.getAddressLine1())
                .addressLine2(orderAddress.getAddressLine2())
                .city(orderAddress.getCity())
                .postalCode(orderAddress.getPostalCode())
                .country(orderAddress.getCountry())
                .createdAt(orderAddress.getCreatedAt())
                .build();
    }
}
