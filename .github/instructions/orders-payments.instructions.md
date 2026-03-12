---
description: Usar cuando se trabaja con pedidos, pagos, carrito, cupones o el ciclo de vida Order/Payment
applyTo: 'src/main/java/es/marcha/backend/modules/order/**'
---

# Skill: Pedidos y Pagos

Módulo ubicado en `modules/order/`. Gestiona el ciclo de vida completo de pedidos y pagos.

## Servicios del módulo

| Servicio | Responsabilidad |
|---|---|
| `OrderService` | Crear órdenes, avanzar estado, snapshot de items |
| `PaymentService` | Crear pagos, transiciones de estado, sincronizar Order |
| `OrderItemsService` | Construir el snapshot inmutable de ítems |
| `OrderAddressService` | Guardar snapshot de dirección en el pedido |
| `StripeService` | Crear PaymentIntent, procesar webhook |

## Ciclo de vida — Payment

```
CREATED → PENDING → AUTHORIZED → SUCCESS → REFUNDED
                 ↘           ↘
                  FAILED       FAILED
CREATED / PENDING / AUTHORIZED → CANCELLED  (cancelación manual)
```

**Estados terminales**: `FAILED`, `EXPIRED`, `CANCELLED`, `REFUNDED`.  
Intentar avanzar desde un estado terminal lanza `OrderException(TERMINAL_STATUS_PAYMENT)` → HTTP 409.

Transiciones válidas del `switch` en `PaymentService.nextPaymentStatus`:
- `CREATED` → solo `PENDING`
- `PENDING` → `AUTHORIZED` o `FAILED`
- `AUTHORIZED` → `SUCCESS` o `FAILED`
- `SUCCESS` → solo `REFUNDED`

## Ciclo de vida — Order

```
CREATED → PAID → PROCESSING → SHIPPED → DELIVERED → RETURNED
       ↘ (cualquier estado no terminal)
        CANCELLED
```

**El estado de Order NO se modifica directamente.** Se recalcula automáticamente llamando a `PaymentService.updateOrderStatusFromPayments(order)` después de cualquier cambio en pagos.

Reglas de recálculo:
1. Si hay un pago `SUCCESS` + un pago `REFUNDED` → `RETURNED`
2. Si hay un pago `SUCCESS` (sin `REFUNDED`) → `PAID`
3. Si todos los pagos son `FAILED` o `CANCELLED` → `CANCELLED`
4. Si hay algún pago en progreso (`CREATED`, `PENDING`, `AUTHORIZED`) → `PROCESSING`
5. Por defecto → `CANCELLED`

Al pasar a `PAID` por primera vez, `handleOrderPaidTransition` genera la factura y envía el email de confirmación (async).

## Creación de una Order — flujo

1. Frontend envía `{ userId, items: [{productId, quantity}], addressId?, couponCode? }`.
2. `OrderService.saveNewOrder` carga precios reales desde `ProductRepository`.
3. Calcula `effectivePrice = discountPrice > 0 ? discountPrice : price`.
4. Construye `OrderItems` como **snapshot inmutable** (copia todos los campos del producto en ese momento).
5. Aplica cupón si se proporciona (`CouponService.applyCoupon`).
6. Persiste `Order` + `OrderItems` + snapshot de dirección.
7. Decrementa stock vía `InventoryService`.
8. Envía email de confirmación async.

**Nunca aceptar `totalAmount` del frontend.** El total siempre lo calcula el backend.

## Snapshot de OrderItems

`OrderItems` es inmutable: copia `name`, `price`, `discountPrice`, `imageUrl`, etc. del producto en el momento de creación.  
Cambios futuros en el catálogo NO afectan a pedidos históricos. No actualizar `OrderItems` nunca.

## Cupones

- `CouponService.applyCoupon(code, totalAmount)` valida el cupón y devuelve el total con descuento.
- Si el cupón no existe, ya fue usado o está expirado, lanza `CouponException`.
- Tras aplicarlo, marcar como usado (`coupon.setUsed(true)`).

## Cancelación y reembolso

- **Cancelar pago** (`cancelPayment`): idempotente si ya está `CANCELLED`. Revierte el `amount` del `totalAmount` de la order.
- **Reembolsar pago** (`refundPayment`): solo si el pago está en `SUCCESS`. Si Stripe está configurado, ejecutar reembolso vía API de Stripe antes de cambiar el estado local.

## Excepciones del módulo

```java
OrderException.DEFAULT                // ORDER_NOT_FOUND
OrderException.DUPLICATE_TRANSACTION  // TRANSACTION_ID_DUPLICATED
OrderException.TERMINAL_STATUS_PAYMENT// PAYMENT_HAS_A_FINAL_STATUS
OrderException.INVALID_STATUS_TRANSITION // PAYMENT_INVALID_STATUS_TRANSITION
OrderException.NOT_VALID_PAYMENT      // NOT_VALID_PAYMENT FOUNDS
```

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| GET | `/orders/users/{id}` | Pedidos de un usuario |
| GET | `/orders/{id}` | Detalle de un pedido |
| POST | `/orders` | Crear pedido |
| POST | `/orders/next-status` | Avanzar estado manual (admin) |
| POST | `/orders/{orderId}/payments` | Registrar pago |
| GET | `/orders/{orderId}/payments/last` | Último pago válido |
| POST | `/orders/payments/{id}/nextStatus` | Avanzar estado de pago |
| POST | `/orders/payments/{id}/cancel` | Cancelar pago |
| POST | `/orders/payments/{id}/refund` | Reembolsar pago |
