package es.marcha.backend.dto.request.payment;

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
public class CreatePaymentIntentRequestDTO {

    private long orderId;
    private String currency;
}
