---
description: Usar cuando se trabaja con la integración de Stripe, PaymentIntent o los webhooks
applyTo: 'src/main/java/es/marcha/backend/modules/order/**/Stripe*'
---

# Skill: Stripe

La integración utiliza el flujo **PaymentIntent** de Stripe. El backend crea el intent y devuelve el `clientSecret` al frontend; el resultado se recibe de forma asíncrona por webhook.

## Flujo completo

```
1. Frontend         → POST /stripe/payment-intent  { orderId }
2. Backend          → Stripe API: crea PaymentIntent por el importe de la Order
3. Backend          → Frontend: { clientSecret, paymentIntentId, localPaymentId, ... }
4. Frontend         → Stripe.js: confirmCardPayment(clientSecret)
5. Stripe           → POST /stripe/webhook  (evento payment_intent.*)
6. Backend          → actualiza Payment + Order en base de datos
```

## Componentes

| Clase | Ubicación | Responsabilidad |
|---|---|---|
| `StripeConfig` | `modules/order/` | Lee `stripe.secret-key` al arrancar y llama a `Stripe.apiKey` |
| `StripeService` | `application/service/` | `createPaymentIntent`, `handleWebhookEvent` |
| `StripeController` | `presentation/controller/` | 2 endpoints: `/stripe/payment-intent` y `/stripe/webhook` |
| `StripePaymentException` | `core/error/exception/` | `STRIPE_CANCEL_FAILED`, `STRIPE_REFUND_FAILED`, etc. |

## Crear PaymentIntent

```java
// StripeService.createPaymentIntent
PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
    .setAmount(amountInCents)           // importe en céntimos
    .setCurrency(stripeCurrency)        // variable de entorno STRIPE_CURRENCY
    .putMetadata("orderId", String.valueOf(orderId))
    .putMetadata("localPaymentId", String.valueOf(localPaymentId))
    .build();

PaymentIntent intent = PaymentIntent.create(params);
```

El `localPaymentId` en los metadatos es **crítico**: permite al webhook asociar el evento de Stripe con el `Payment` local.

## Webhook — seguridad

`POST /stripe/webhook` es **público** (sin JWT). La autenticidad se verifica obligatoriamente con la firma:

```java
Event event = Webhook.constructEvent(
    payload,                              // body raw como String
    sigHeader,                            // cabecera "Stripe-Signature"
    stripeWebhookSecret                   // variable STRIPE_WEBHOOK_SECRET
);
```

Si la verificación falla → responder `400 Bad Request`. **Nunca procesar el evento si la firma es inválida.**

⚠️ **No añadir autenticación JWT a este endpoint** — Stripe no envía tokens.  
⚠️ **No eliminar ni debilitar la verificación de firma** bajo ninguna circunstancia.

## Eventos gestionados

| Evento Stripe | Acción en backend |
|---|---|
| `payment_intent.succeeded` | `nextPaymentStatus(PENDING → AUTHORIZED → SUCCESS)` → `updateOrderStatusFromPayments` |
| `payment_intent.payment_failed` | `nextPaymentStatus(→ FAILED)` → `updateOrderStatusFromPayments` |
| `payment_intent.canceled` | `cancelPayment(paymentId)` → `updateOrderStatusFromPayments` |
| `charge.refunded` | `refundPayment(paymentId)` → `updateOrderStatusFromPayments` |

El `paymentId` local se extrae de `event.getDataObjectDeserializer().getObject().get().getMetadata().get("localPaymentId")`.

## Variables de entorno requeridas

```env
STRIPE_SECRET_KEY=sk_test_...       # clave secreta de Stripe
STRIPE_WEBHOOK_SECRET=whsec_...     # secreto del webhook endpoint
STRIPE_CURRENCY=eur                 # moneda por defecto
```

Obtener en: [dashboard.stripe.com/apikeys](https://dashboard.stripe.com/apikeys)  
Webhook secret: crear endpoint en [dashboard.stripe.com/webhooks](https://dashboard.stripe.com/webhooks) → `POST /stripe/webhook`.

## Tests del webhook

Al testear el webhook localmente usar **Stripe CLI**:
```bash
stripe listen --forward-to localhost:8080/stripe/webhook
stripe trigger payment_intent.succeeded
```

Para tests unitarios, mockear `Webhook.constructEvent` o usar un payload firmado de prueba.
No testear el webhook con `@SpringBootTest` completo sin mockear Stripe (requiere red real).
