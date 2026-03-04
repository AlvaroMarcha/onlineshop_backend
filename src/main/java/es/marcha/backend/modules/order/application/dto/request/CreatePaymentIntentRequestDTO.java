package es.marcha.backend.modules.order.application.dto.request;

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
