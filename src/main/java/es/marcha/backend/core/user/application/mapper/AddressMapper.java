package es.marcha.backend.core.user.application.mapper;

import es.marcha.backend.core.user.application.dto.response.AddressResponseDTO;
import es.marcha.backend.core.user.domain.model.Address;

public class AddressMapper {
    public static AddressResponseDTO toAddressdDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .isDefault(address.isDefault())
                .type(address.getType())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .country(address.getCountry())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
