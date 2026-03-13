package es.marcha.backend.modules.order.application.dto.response;

import java.time.LocalDateTime;

import es.marcha.backend.modules.order.domain.enums.PaymentStatus;
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
public class PaymentResponseDTO {
    // Attribs
    private long id;
    private long orderId;
    private PaymentStatus status;
    private double amount;
    private String provider;
    private String transactionId;
    private LocalDateTime createdAt;
}
