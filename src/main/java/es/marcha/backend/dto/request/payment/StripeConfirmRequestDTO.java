package es.marcha.backend.dto.request.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la petición de confirmación de pago con Stripe.
 * Utilizado por el endpoint POST /stripe/confirm
 */
@Builder
@Getter
@Setter
public class StripeConfirmRequestDTO {

    /**
     * ID del PaymentIntent de Stripe a confirmar
     */
    private String paymentIntentId;

    /**
     * ID de la dirección de envío seleccionada (opcional)
     */
    private Long shippingAddressId;

    /**
     * Código de cupón de descuento a aplicar (opcional)
     */
    private String couponCode;
}
