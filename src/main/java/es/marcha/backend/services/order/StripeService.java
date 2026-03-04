package es.marcha.backend.services.order;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import es.marcha.backend.core.config.StripeConfig;
import es.marcha.backend.dto.response.payment.StripePaymentIntentResponseDTO;
import es.marcha.backend.core.error.exception.StripePaymentException;
import es.marcha.backend.core.shared.domain.enums.PaymentStatus;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.Payment;
import es.marcha.backend.repository.order.PaymentRepository;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private static final String PROVIDER_NAME = "STRIPE";

    @Autowired
    private StripeConfig stripeConfig;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    /**
     * Crea un PaymentIntent en Stripe para la orden indicada y persiste un registro
     * local
     * de {@link Payment} con estado {@link PaymentStatus#CREATED}.
     *
     * El frontend utiliza el {@code clientSecret} devuelto con
     * {@code stripe.confirmPayment()}
     * de Stripe.js para completar el pago en el lado del cliente.
     *
     * Nota: se guarda el Payment directamente mediante {@link PaymentRepository} en
     * lugar de
     * {@code PaymentService.savePayment} para evitar que el importe se sume dos
     * veces
     * al {@code totalAmount} de la orden (ya calculado desde los
     * {@code OrderItems}).
     *
     * @param orderId  ID de la orden a pagar.
     * @param currency Código ISO 4217 (p.ej. "eur", "usd"). Si es nulo o vacío se
     *                 usa
     *                 el valor de {@code stripe.currency} en
     *                 application.properties.
     * @return {@link StripePaymentIntentResponseDTO} con el clientSecret e
     *         identificadores.
     * @throws StripePaymentException si la llamada a la API de Stripe falla.
     */
    @Transactional
    public StripePaymentIntentResponseDTO createPaymentIntent(long orderId, String currency) {
        Order order = orderService.getOrderByIdHandler(orderId);

        String resolvedCurrency = (currency != null && !currency.isBlank())
                ? currency.toLowerCase()
                : stripeConfig.getCurrency();

        long amountInCents = Math.round(order.getTotalAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(resolvedCurrency)
                .putMetadata("orderId", String.valueOf(orderId))
                .build();

        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(params);
        } catch (StripeException e) {
            log.error("Stripe PaymentIntent creation failed for orderId={}: {}", orderId, e.getMessage());
            throw new StripePaymentException(StripePaymentException.FAILED_CREATE_INTENT);
        }

        if (paymentRepository.existsByTransactionId(intent.getId())) {
            Payment existing = paymentRepository.findByTransactionId(intent.getId())
                    .orElseThrow(() -> new StripePaymentException(StripePaymentException.PAYMENT_NOT_FOUND));
            log.warn("PaymentIntent {} already registered as local payment id={}", intent.getId(), existing.getId());
            return buildResponseDTO(intent, existing);
        }

        Payment payment = Payment.builder()
                .order(order)
                .status(PaymentStatus.CREATED)
                .amount(order.getTotalAmount())
                .provider(PROVIDER_NAME)
                .transactionId(intent.getId())
                .createdAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Stripe payment created: localId={}, intentId={}, orderId={}, amount={} {}",
                payment.getId(), intent.getId(), orderId, order.getTotalAmount(), resolvedCurrency);

        return buildResponseDTO(intent, payment);
    }

    /**
     * Procesa un evento de webhook enviado por Stripe.
     *
     * Verifica la firma del evento y enruta al manejador correspondiente para
     * mantener el estado local del {@link Payment} sincronizado con Stripe:
     * <ul>
     * <li>{@code payment_intent.succeeded} → {@link PaymentStatus#SUCCESS}</li>
     * <li>{@code payment_intent.payment_failed} → {@link PaymentStatus#FAILED}</li>
     * <li>{@code payment_intent.canceled} → {@link PaymentStatus#CANCELLED}</li>
     * <li>{@code charge.refunded} → {@link PaymentStatus#REFUNDED}</li>
     * </ul>
     *
     * @param payload   Cuerpo crudo de la petición de Stripe (no debe
     *                  pre-procesarse).
     * @param sigHeader Valor de la cabecera {@code Stripe-Signature}.
     * @throws StripePaymentException si la firma del webhook no es válida.
     */
    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            throw new StripePaymentException(StripePaymentException.INVALID_WEBHOOK_SIGNATURE);
        }

        log.info("Stripe webhook received: type={}, id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            case "payment_intent.canceled" -> handlePaymentCancelled(event);
            case "charge.refunded" -> handleChargeRefunded(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handlePaymentSucceeded(Event event) {
        PaymentIntent intent = extractPaymentIntent(event);
        if (intent == null)
            return;
        updateLocalPaymentStatus(intent.getId(), PaymentStatus.SUCCESS);
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent intent = extractPaymentIntent(event);
        if (intent == null)
            return;
        updateLocalPaymentStatus(intent.getId(), PaymentStatus.FAILED);
    }

    private void handlePaymentCancelled(Event event) {
        PaymentIntent intent = extractPaymentIntent(event);
        if (intent == null)
            return;
        updateLocalPaymentStatus(intent.getId(), PaymentStatus.CANCELLED);
    }

    private void handleChargeRefunded(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isEmpty()) {
            log.warn("Could not deserialize charge.refunded event data for eventId={}", event.getId());
            return;
        }
        Charge charge = (Charge) deserializer.getObject().get();
        if (charge.getPaymentIntent() == null) {
            log.warn("charge.refunded event has no paymentIntentId — eventId={}", event.getId());
            return;
        }
        updateLocalPaymentStatus(charge.getPaymentIntent(), PaymentStatus.REFUNDED);
    }

    /**
     * Actualiza el estado del {@link Payment} local asociado al PaymentIntent de
     * Stripe indicado.
     *
     * Los eventos de webhook son autoritativos: el estado se actualiza directamente
     * sin pasar
     * por la máquina de estados de {@link PaymentService}, para gestionar
     * correctamente
     * reintentos y condiciones de carrera.
     *
     * @param intentId     ID del PaymentIntent de Stripe.
     * @param targetStatus Nuevo estado a aplicar al pago local.
     */
    @Transactional
    protected void updateLocalPaymentStatus(String intentId, PaymentStatus targetStatus) {
        Payment payment = paymentRepository.findByTransactionId(intentId).orElse(null);
        if (payment == null) {
            log.warn("No local payment found for intentId={} — status {} not applied", intentId, targetStatus);
            return;
        }
        payment.setStatus(targetStatus);
        paymentRepository.save(payment);
        log.info("Payment id={} status updated to {} via Stripe webhook (intentId={})",
                payment.getId(), targetStatus, intentId);
    }

    /**
     * Confirma el pago verificando el estado del PaymentIntent en Stripe y
     * actualizando el estado local del Payment correspondiente.
     *
     * @param intentId ID del PaymentIntent de Stripe
     */
    @Transactional
    public void confirmPayment(String intentId) {
        PaymentIntent intent;
        try {
            intent = PaymentIntent.retrieve(intentId);
        } catch (StripeException e) {
            log.error("Failed to retrieve PaymentIntent {}: {}", intentId, e.getMessage());
            throw new StripePaymentException(StripePaymentException.FAILED_RETRIEVE_INTENT);
        }

        String stripeStatus = intent.getStatus();
        Payment payment = paymentRepository.findByTransactionId(intentId).orElse(null);
        if (payment == null) {
            log.warn("No local payment found for intentId={}", intentId);
            throw new StripePaymentException(StripePaymentException.PAYMENT_NOT_FOUND);
        }

        try {
            if ("succeeded".equalsIgnoreCase(stripeStatus)) {
                if (payment.getStatus() == PaymentStatus.CREATED) {
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.PENDING);
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.AUTHORIZED);
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.SUCCESS);
                } else if (payment.getStatus() == PaymentStatus.PENDING) {
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.AUTHORIZED);
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.SUCCESS);
                } else if (payment.getStatus() == PaymentStatus.AUTHORIZED) {
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.SUCCESS);
                }
            } else if ("processing".equalsIgnoreCase(stripeStatus)
                    || "requires_action".equalsIgnoreCase(stripeStatus)
                    || "requires_payment_method".equalsIgnoreCase(stripeStatus)) {
                if (payment.getStatus() == PaymentStatus.CREATED) {
                    paymentService.nextPaymentStatus(payment.getId(), PaymentStatus.PENDING);
                }
            } else if ("canceled".equalsIgnoreCase(stripeStatus)) {
                paymentService.cancelPayment(payment.getId());
            }
        } catch (Exception e) {
            log.error("Error advancing payment status for local payment id={} intent={}: {}",
                    payment.getId(), intentId, e.getMessage());
            throw new StripePaymentException(StripePaymentException.FAILED_UPDATE_PAYMENT);
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isEmpty()) {
            log.warn("Could not deserialize PaymentIntent from event type={} id={}", event.getType(), event.getId());
            throw new StripePaymentException(StripePaymentException.FAILED_DESERIALIZE_EVENT);
        }
        return (PaymentIntent) deserializer.getObject().get();
    }

    private StripePaymentIntentResponseDTO buildResponseDTO(PaymentIntent intent, Payment payment) {
        return StripePaymentIntentResponseDTO.builder()
                .paymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .localPaymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .currency(intent.getCurrency())
                .build();
    }
}
