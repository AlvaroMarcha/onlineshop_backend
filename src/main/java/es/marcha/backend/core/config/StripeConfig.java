package es.marcha.backend.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeConfig {

    private String secretKey;
    private String webhookSecret;
    private String currency = "eur";

    /**
     * Inicializa el SDK de Stripe con la clave secreta configurada.
     * Spring lo invoca automáticamente tras enlazar las propiedades.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
