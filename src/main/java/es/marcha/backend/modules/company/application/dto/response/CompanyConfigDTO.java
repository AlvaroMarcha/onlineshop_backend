package es.marcha.backend.modules.company.application.dto.response;

import java.time.LocalDateTime;

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
public class CompanyConfigDTO {

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
    private String logoPath;
    private LocalDateTime updatedAt;
}
