package es.marcha.backend.modules.order.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import es.marcha.backend.core.shared.domain.enums.OrderStatus;
import es.marcha.backend.core.user.domain.model.Role;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.modules.order.domain.enums.PaymentStatus;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.domain.model.Payment;

/**
 * Tests de repositorio para PaymentRepository.
 *
 * Verifica:
 * - existsByTransactionId: retorna true/false según exista el transactionId
 * - findAllByOrderId: devuelve todos los pagos de una orden
 * - findByTransactionId: retorna Optional correcto
 *
 * Usa H2 en memoria con TestEntityManager para preparar el grafo de entidades
 * (Role → User → Order → Payment).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@DisplayName("PaymentRepository — consultas custom")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PaymentRepository paymentRepository;

    private Order order;

    @BeforeEach
    void setUp() {
        order = createOrder();
        em.persistAndFlush(Payment.builder()
                .order(order)
                .status(PaymentStatus.CREATED)
                .amount(100.0)
                .provider("stripe")
                .transactionId("tx_abc123")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // =========================================================================
    // existsByTransactionId
    // =========================================================================

    @Nested
    @DisplayName("existsByTransactionId")
    class ExistsByTransactionIdTests {

        @Test
        @DisplayName("transactionId existente → retorna true")
        void existsByTransactionId_existente_retornaTrue() {
            assertTrue(paymentRepository.existsByTransactionId("tx_abc123"));
        }

        @Test
        @DisplayName("transactionId no existente → retorna false")
        void existsByTransactionId_noExistente_retornaFalse() {
            assertFalse(paymentRepository.existsByTransactionId("tx_inexistente"));
        }
    }

    // =========================================================================
    // findAllByOrderId
    // =========================================================================

    @Nested
    @DisplayName("findAllByOrderId")
    class FindAllByOrderIdTests {

        @Test
        @DisplayName("order con un pago → devuelve lista con un elemento")
        void findAllByOrderId_conPago_devuelveLista() {
            List<Payment> result = paymentRepository.findAllByOrderId(order.getId());
            assertEquals(1, result.size());
            assertEquals("tx_abc123", result.get(0).getTransactionId());
        }

        @Test
        @DisplayName("order sin pagos → devuelve lista vacía")
        void findAllByOrderId_sinPagos_devuelveListaVacia() {
            Order otraOrden = createOrder();
            List<Payment> result = paymentRepository.findAllByOrderId(otraOrden.getId());
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // findByTransactionId
    // =========================================================================

    @Nested
    @DisplayName("findByTransactionId")
    class FindByTransactionIdTests {

        @Test
        @DisplayName("transactionId existente → retorna Optional presente con datos correctos")
        void findByTransactionId_existente_retornaOptional() {
            Optional<Payment> result = paymentRepository.findByTransactionId("tx_abc123");
            assertTrue(result.isPresent());
            assertEquals("stripe", result.get().getProvider());
            assertEquals(100.0, result.get().getAmount());
        }

        @Test
        @DisplayName("transactionId no existente → retorna Optional vacío")
        void findByTransactionId_noExistente_retornaVacio() {
            Optional<Payment> result = paymentRepository.findByTransactionId("tx_noexiste");
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Order createOrder() {
        // Cada llamada genera Role y User únicos para evitar conflictos de unicidad
        long ts = System.nanoTime();
        Role role = em.persistAndFlush(Role.builder()
                .name("ROLE_TEST_" + ts)
                .createdAt(LocalDateTime.now())
                .build());
        User user = em.persistAndFlush(User.builder()
                .name("Test")
                .surname("User")
                .username("testuser_pay_" + ts)
                .email("testpay_" + ts + "@test.com")
                .password("encoded")
                .phone("600000000")
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());
        return em.persistAndFlush(Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(100.0)
                .paymentMethod("STRIPE")
                .discountAmount(0.0)
                .build());
    }
}
