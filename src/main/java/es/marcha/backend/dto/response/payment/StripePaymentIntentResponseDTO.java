package es.marcha.backend.dto.response.payment;

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
public class StripePaymentIntentResponseDTO {

    private String paymentIntentId;
    private String clientSecret;
    private long localPaymentId;
    private long orderId;
    private double amount;
    private String currency;
}
