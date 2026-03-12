package es.marcha.backend.modules.order.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.shared.domain.enums.OrderStatus;
import es.marcha.backend.modules.order.domain.enums.PaymentStatus;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.domain.model.Payment;
import es.marcha.backend.modules.order.infrastructure.persistence.PaymentRepository;

/**
 * Tests unitarios para PaymentService — método updateOrderStatusFromPayments.
 *
 * El estado de la orden se recalcula automáticamente desde el estado
 * agregado de sus pagos. Nunca se modifica manualmente.
 *
 * Reglas de agregación:
 * - Todos SUCCESS → ORDER COMPLETED
 * - Alguno FAILED/CANCELLED/EXPIRED → ORDER CANCELLED
 * - Alguno PENDING/AUTHORIZED → ORDER PROCESSING
 * - Todos REFUNDED → ORDER REFUNDED
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — updateOrderStatusFromPayments")
class PaymentServiceUpdateOrderTest {

    @Mock
    private PaymentRepository pRepository;

    @Mock
    private OrderService oService;

    @InjectMocks
    private PaymentService paymentService;

    // =========================================================================
    // Agregación de estados
    // =========================================================================

    @Nested
    @DisplayName("Agregación de estados de pagos → estado de orden")
    class AgregacionEstados {

        @Test
        @DisplayName("pago SUCCESS → estado orden PAID")
        void todosSuccess_ordenPaid() {
            Order order = buildOrder(1L);
            Payment payment = buildPayment(1L, PaymentStatus.SUCCESS, order);

            when(pRepository.findAllByOrderId(1L)).thenReturn(List.of(payment));

            paymentService.updateOrderStatusFromPayments(order);

            assertEquals(OrderStatus.PAID, order.getStatus());
            verify(oService).saveOrder(order);
        }

        @Test
        @DisplayName("pago FAILED → estado orden CANCELLED")
        void pagoFailed_ordenCancelled() {
            Order order = buildOrder(2L);
            Payment payment = buildPayment(2L, PaymentStatus.FAILED, order);

            when(pRepository.findAllByOrderId(2L)).thenReturn(List.of(payment));

            paymentService.updateOrderStatusFromPayments(order);

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("pago CANCELLED → estado orden CANCELLED")
        void pagoCancelled_ordenCancelled() {
            Order order = buildOrder(3L);
            Payment payment = buildPayment(3L, PaymentStatus.CANCELLED, order);

            when(pRepository.findAllByOrderId(3L)).thenReturn(List.of(payment));

            paymentService.updateOrderStatusFromPayments(order);

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("pago PENDING → estado orden PROCESSING")
        void pagoPending_ordenProcessing() {
            Order order = buildOrder(4L);
            Payment payment = buildPayment(4L, PaymentStatus.PENDING, order);

            when(pRepository.findAllByOrderId(4L)).thenReturn(List.of(payment));

            paymentService.updateOrderStatusFromPayments(order);

            assertEquals(OrderStatus.PROCESSING, order.getStatus());
        }

        @Test
        @DisplayName("pago SUCCESS luego REFUNDED → estado orden RETURNED")
        void pagoRefunded_ordenReturned() {
            Order order = buildOrder(5L);
            Payment payment1 = buildPayment(5L, PaymentStatus.SUCCESS, order);
            Payment payment2 = buildPayment(6L, PaymentStatus.REFUNDED, order);

            when(pRepository.findAllByOrderId(5L)).thenReturn(List.of(payment1, payment2));

            paymentService.updateOrderStatusFromPayments(order);

            assertEquals(OrderStatus.RETURNED, order.getStatus());
        }

        @Test
        @DisplayName("sin pagos → estado orden CANCELLED")
        void sinPagos_ordenCancelled() {
            Order order = buildOrder(99L);
            when(pRepository.findAllByOrderId(99L)).thenReturn(List.of());

            paymentService.updateOrderStatusFromPayments(order);

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            verify(oService).saveOrder(order);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Order buildOrder(long id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        return order;
    }

    private Payment buildPayment(long id, PaymentStatus status, Order order) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setStatus(status);
        payment.setOrder(order);
        return payment;
    }
}
