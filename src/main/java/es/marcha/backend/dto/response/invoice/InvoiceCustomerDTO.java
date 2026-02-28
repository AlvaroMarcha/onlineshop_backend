package es.marcha.backend.dto.response.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class InvoiceCustomerDTO {

    private String name;
    private String nif;
    private String address;
    private String postalCode;
    private String city;
    private String country;
}
