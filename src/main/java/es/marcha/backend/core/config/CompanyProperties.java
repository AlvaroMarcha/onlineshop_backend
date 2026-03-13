package es.marcha.backend.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.company")
@Getter
@Setter
public class CompanyProperties {

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
}
