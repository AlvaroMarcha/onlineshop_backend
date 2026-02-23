package es.marcha.backend.dto.response;

import java.time.LocalDateTime;

import es.marcha.backend.model.enums.AddressesType;
import es.marcha.backend.model.order.Order;
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
public class OrderAddrResponseDTO {
    private long id;
    private Order order;
    private AddressesType type;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime createdAt;

}
