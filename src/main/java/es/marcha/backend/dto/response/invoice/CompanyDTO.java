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
public class CompanyDTO {

    private String logoBase64;
    private String logoMime;
    private String name;
    private String nif;
    private String address;
    private String email;
    private String phone;
    private String iban;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String textColor;
}
