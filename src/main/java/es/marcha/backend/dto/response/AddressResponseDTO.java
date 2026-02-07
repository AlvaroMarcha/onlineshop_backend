package es.marcha.backend.dto.response;

import java.time.LocalDateTime;

import es.marcha.backend.model.enums.TypeAddresses;
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
public class AddressResponseDTO {
    private long id;
    private boolean isDefault;
    private TypeAddresses type;
    private String addressLine1;
    private String addressLine2;
    private String country;
    private String city;
    private String postalCode;
    private LocalDateTime createdAt;

}
