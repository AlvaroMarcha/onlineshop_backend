package es.marcha.backend.modules.order.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.OrderException;
import es.marcha.backend.modules.order.domain.enums.PaymentStatus;
import es.marcha.backend.modules.order.domain.model.Payment;
import es.marcha.backend.modules.order.infrastructure.persistence.PaymentRepository;

/**
 * Tests unitarios para PaymentService.
 *
 * Verifica el funcionamiento de la máquina de estados de pagos:
 * - Transiciones válidas
 * - Transiciones inválidas (lanzar OrderException con
 * INVALID_STATUS_TRANSITION)
 * - Estados terminales (lanzar OrderException con TERMINAL_STATUS_PAYMENT)
 * - Pago inexistente (lanzar OrderException)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — máquina de estados")
class PaymentServiceTest {

    @Mock
    private PaymentRepository pRepository;

    @Mock
    private OrderService oService;

    @InjectMocks
    private PaymentService paymentService;

    // =========================================================================
    // Transiciones válidas
    // =========================================================================

    @Nested
    @DisplayName("Transiciones válidas")
    class TransicionesValidas {

        @Test
        @DisplayName("CREATED → PENDING")
        void givenCreatedPayment_whenAdvanceToPending_thenStatusIsPending() {
            Payment payment = buildPayment(1L, PaymentStatus.CREATED);
            when(pRepository.findById(1L)).thenReturn(Optional.of(payment));

            PaymentStatus result = paymentService.nextPaymentStatus(1L, PaymentStatus.PENDING);

            assertEquals(PaymentStatus.PENDING, result);
            verify(pRepository).save(payment);
        }

        @Test
        @DisplayName("PENDING → AUTHORIZED")
        void givenPendingPayment_whenAdvanceToAuthorized_thenStatusIsAuthorized() {
            Payment payment = buildPayment(2L, PaymentStatus.PENDING);
            when(pRepository.findById(2L)).thenReturn(Optional.of(payment));

            PaymentStatus result = paymentService.nextPaymentStatus(2L, PaymentStatus.AUTHORIZED);

            assertEquals(PaymentStatus.AUTHORIZED, result);
            verify(pRepository).save(payment);
        }

        @Test
        @DisplayName("PENDING → FAILED")
        void givenPendingPayment_whenAdvanceToFailed_thenStatusIsFailed() {
            Payment payment = buildPayment(3L, PaymentStatus.PENDING);
            when(pRepository.findById(3L)).thenReturn(Optional.of(payment));

            PaymentStatus result = paymentService.nextPaymentStatus(3L, PaymentStatus.FAILED);

            assertEquals(PaymentStatus.FAILED, result);
        }

        @Test
        @DisplayName("AUTHORIZED → SUCCESS")
        void givenAuthorizedPayment_whenAdvanceToSuccess_thenStatusIsSuccess() {
            Payment payment = buildPayment(4L, PaymentStatus.AUTHORIZED);
            when(pRepository.findById(4L)).thenReturn(Optional.of(payment));

            PaymentStatus result = paymentService.nextPaymentStatus(4L, PaymentStatus.SUCCESS);

            assertEquals(PaymentStatus.SUCCESS, result);
        }

        @Test
        @DisplayName("AUTHORIZED → FAILED")
        void givenAuthorizedPayment_whenAdvanceToFailed_thenStatusIsFailed() {
            Payment payment = buildPayment(5L, PaymentStatus.AUTHORIZED);
            when(pRepository.findById(5L)).thenReturn(Optional.of(payment));

            PaymentStatus result = paymentService.nextPaymentStatus(5L, PaymentStatus.FAILED);

            assertEquals(PaymentStatus.FAILED, result);
        }

        @Test
        @DisplayName("SUCCESS → REFUNDED")
        void givenSuccessPayment_whenAdvanceToRefunded_thenStatusIsRefunded() {
            Payment payment = buildPayment(6L, PaymentStatus.SUCCESS);
            when(pRepository.findById(6L)).thenReturn(Optional.of(payment));

            PaymentStatus result = paymentService.nextPaymentStatus(6L, PaymentStatus.REFUNDED);

            assertEquals(PaymentStatus.REFUNDED, result);
        }
    }

    // =========================================================================
    // Transiciones inválidas
    // =========================================================================

    @Nested
    @DisplayName("Transiciones inválidas — deben lanzar INVALID_STATUS_TRANSITION")
    class TransicionesInvalidas {

        @Test
        @DisplayName("CREATED → AUTHORIZED (saltarse PENDING) lanza OrderException")
        void givenCreatedPayment_whenAdvanceToAuthorized_thenThrowsInvalidTransition() {
            Payment payment = buildPayment(10L, PaymentStatus.CREATED);
            when(pRepository.findById(10L)).thenReturn(Optional.of(payment));

            OrderException ex = assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(10L, PaymentStatus.AUTHORIZED));

            assertEquals(OrderException.INVALID_STATUS_TRANSITION, ex.getMessage());
        }

        @Test
        @DisplayName("CREATED → SUCCESS (saltarse pasos) lanza OrderException")
        void givenCreatedPayment_whenAdvanceToSuccess_thenThrowsInvalidTransition() {
            Payment payment = buildPayment(11L, PaymentStatus.CREATED);
            when(pRepository.findById(11L)).thenReturn(Optional.of(payment));

            OrderException ex = assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(11L, PaymentStatus.SUCCESS));

            assertEquals(OrderException.INVALID_STATUS_TRANSITION, ex.getMessage());
        }

        @Test
        @DisplayName("AUTHORIZED → PENDING (retroceder) lanza OrderException")
        void givenAuthorizedPayment_whenAdvanceToPending_thenThrowsInvalidTransition() {
            Payment payment = buildPayment(12L, PaymentStatus.AUTHORIZED);
            when(pRepository.findById(12L)).thenReturn(Optional.of(payment));

            assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(12L, PaymentStatus.PENDING));
        }

        @Test
        @DisplayName("SUCCESS → PENDING (retroceder) lanza OrderException")
        void givenSuccessPayment_whenAdvanceToPending_thenThrowsInvalidTransition() {
            Payment payment = buildPayment(13L, PaymentStatus.SUCCESS);
            when(pRepository.findById(13L)).thenReturn(Optional.of(payment));

            assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(13L, PaymentStatus.PENDING));
        }
    }

    // =========================================================================
    // Estados terminales — no se puede avanzar desde ellos
    // =========================================================================

    @Nested
    @DisplayName("Estados terminales — deben lanzar TERMINAL_STATUS_PAYMENT")
    class EstadosTerminales {

        @Test
        @DisplayName("FAILED es terminal: cualquier avance lanza OrderException")
        void givenFailedPayment_whenAdvanceToAnyStatus_thenThrowsTerminal() {
            Payment payment = buildPayment(20L, PaymentStatus.FAILED);
            when(pRepository.findById(20L)).thenReturn(Optional.of(payment));

            OrderException ex = assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(20L, PaymentStatus.PENDING));

            assertTrue(ex.getMessage().contains(OrderException.TERMINAL_STATUS_PAYMENT));
        }

        @Test
        @DisplayName("CANCELLED es terminal: cualquier avance lanza OrderException")
        void givenCancelledPayment_whenAdvanceToAnyStatus_thenThrowsTerminal() {
            Payment payment = buildPayment(21L, PaymentStatus.CANCELLED);
            when(pRepository.findById(21L)).thenReturn(Optional.of(payment));

            OrderException ex = assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(21L, PaymentStatus.PENDING));

            assertTrue(ex.getMessage().contains(OrderException.TERMINAL_STATUS_PAYMENT));
        }

        @Test
        @DisplayName("REFUNDED es terminal: cualquier avance lanza OrderException")
        void givenRefundedPayment_whenAdvanceToAnyStatus_thenThrowsTerminal() {
            Payment payment = buildPayment(22L, PaymentStatus.REFUNDED);
            when(pRepository.findById(22L)).thenReturn(Optional.of(payment));

            OrderException ex = assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(22L, PaymentStatus.SUCCESS));

            assertTrue(ex.getMessage().contains(OrderException.TERMINAL_STATUS_PAYMENT));
        }

        @Test
        @DisplayName("EXPIRED es terminal: cualquier avance lanza OrderException")
        void givenExpiredPayment_whenAdvanceToAnyStatus_thenThrowsTerminal() {
            Payment payment = buildPayment(23L, PaymentStatus.EXPIRED);
            when(pRepository.findById(23L)).thenReturn(Optional.of(payment));

            assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(23L, PaymentStatus.PENDING));
        }
    }

    // =========================================================================
    // Casos de error — payment no encontrado
    // =========================================================================

    @Nested
    @DisplayName("Pago no encontrado")
    class PagoNoEncontrado {

        @Test
        @DisplayName("ID inexistente lanza OrderException (DEFAULT)")
        void givenNonExistentPaymentId_whenAdvanceStatus_thenThrowsOrderException() {
            when(pRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(OrderException.class,
                    () -> paymentService.nextPaymentStatus(99L, PaymentStatus.PENDING));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Construye un Payment de prueba con solo los campos necesarios para los tests
     * de transición de estado. No requiere Order ni campos DB-obligatorios ya que
     * no se persiste en base de datos.
     */
    private Payment buildPayment(long id, PaymentStatus status) {
        return Payment.builder()
                .id(id)
                .status(status)
                .amount(49.99)
                .createdAt(LocalDateTime.now())
                .transactionId("txn_test_" + id)
                .provider("stripe")
                .build();
    }
}
