package es.marcha.backend.modules.order.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.modules.order.application.dto.request.CreatePaymentIntentRequestDTO;
import es.marcha.backend.modules.order.application.dto.request.StripeConfirmRequestDTO;
import es.marcha.backend.modules.order.application.dto.response.StripePaymentIntentResponseDTO;
import es.marcha.backend.core.error.exception.StripePaymentException;
import es.marcha.backend.modules.order.application.service.StripeService;

@RestController
@RequestMapping("/stripe")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    /**
     * Crea un PaymentIntent en Stripe para la orden especificada.
     *
     * El frontend utiliza el {@code clientSecret} devuelto con
     * {@code stripe.confirmPayment()}
     * de Stripe.js para completar el pago en el lado del cliente.
     *
     * @param request contiene {@code orderId} y, opcionalmente, {@code currency}.
     * @return 201 CREATED con {@link StripePaymentIntentResponseDTO}.
     */
    @PostMapping("/payment-intent")
    public ResponseEntity<StripePaymentIntentResponseDTO> createPaymentIntent(
            @RequestBody CreatePaymentIntentRequestDTO request) {
        StripePaymentIntentResponseDTO response = stripeService.createPaymentIntent(
                request.getOrderId(), request.getCurrency());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint de webhook de Stripe.
     *
     * Recibe eventos de Stripe ({@code payment_intent.succeeded},
     * {@code payment_intent.payment_failed}, {@code payment_intent.canceled},
     * {@code charge.refunded}) y actualiza el estado del pago local en
     * consecuencia.
     *
     * Este endpoint es público: Stripe no puede enviar un token JWT.
     * La firma de la petición se verifica mediante {@code STRIPE_WEBHOOK_SECRET}.
     *
     * @param payload   Cuerpo JSON crudo enviado por Stripe.
     * @param sigHeader Valor de la cabecera {@code Stripe-Signature}.
     * @return 200 OK si se procesa correctamente; 400 BAD REQUEST si la firma es
     *         inválida.
     */
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripeService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed");
        } catch (StripePaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Confirma el pago verificando el estado del PaymentIntent en Stripe.
     *
     * Este endpoint es llamado por el frontend después de que Stripe.js confirme
     * el pago del cliente. Verifica el estado del PaymentIntent en Stripe y
     * actualiza el estado del pago local en consecuencia.
     *
     * @param request contiene {@code paymentIntentId} y opcionalmente
     *                {@code shippingAddressId} y {@code couponCode}.
     * @param auth    objeto de autenticación del usuario.
     * @return 200 OK si el pago se confirma correctamente; 400 BAD REQUEST si hay
     *         un error.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
            @RequestBody StripeConfirmRequestDTO request,
            Authentication auth) {
        try {
            stripeService.confirmPayment(request.getPaymentIntentId());
            return ResponseEntity.ok("Payment confirmed successfully");
        } catch (StripePaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
