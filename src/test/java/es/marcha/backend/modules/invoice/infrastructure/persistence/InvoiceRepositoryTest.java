package es.marcha.backend.modules.invoice.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import es.marcha.backend.modules.invoice.domain.enums.InvoiceStatus;
import es.marcha.backend.modules.invoice.domain.model.Invoice;
import es.marcha.backend.modules.order.domain.model.Order;

/**
 * Tests de repositorio para InvoiceRepository.
 *
 * Verifica:
 * - findLastByYearPrefix: devuelve números de factura ordenados DESC para un
 * año
 * - findByOrderId: retorna la factura de un pedido (idempotencia)
 * - findAllByUserId: devuelve todas las facturas de un usuario
 *
 * Usa H2 en memoria con TestEntityManager para preparar el grafo de entidades
 * (Role → User → Order → Invoice).
 * Cada Order tiene un único Invoice (restricción unique en order_id).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@DisplayName("InvoiceRepository — consultas custom")
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // =========================================================================
    // findLastByYearPrefix
    // =========================================================================

    @Nested
    @DisplayName("findLastByYearPrefix")
    class FindLastByYearPrefixTests {

        @Test
        @DisplayName("sin facturas del año → devuelve lista vacía")
        void findLastByYearPrefix_sinFacturas_devuelveListaVacia() {
            List<String> result = invoiceRepository.findLastByYearPrefix("INV-2020-");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("con facturas del año → devuelve números ordenados DESC (el primero es el mayor)")
        void findLastByYearPrefix_conFacturas_devuelveOrdenadoDesc() {
            User user = createUser();
            Order order1 = createOrder(user);
            Order order2 = createOrder(user);
            em.persistAndFlush(buildInvoice(order1, user, "INV-2024-000001"));
            em.persistAndFlush(buildInvoice(order2, user, "INV-2024-000002"));

            List<String> result = invoiceRepository.findLastByYearPrefix("INV-2024-");

            assertEquals(2, result.size());
            // El primer elemento debe ser el mayor (ordenación alfanumérica DESC)
            assertEquals("INV-2024-000002", result.get(0));
            assertEquals("INV-2024-000001", result.get(1));
        }

        @Test
        @DisplayName("facturas de otro año → no aparecen en resultado")
        void findLastByYearPrefix_otroAnio_noAparece() {
            User user = createUser();
            Order order = createOrder(user);
            em.persistAndFlush(buildInvoice(order, user, "INV-2023-000001"));

            List<String> result = invoiceRepository.findLastByYearPrefix("INV-2024-");
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // findByOrderId
    // =========================================================================

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderIdTests {

        @Test
        @DisplayName("invoice existente para order → retorna Optional presente con número correcto")
        void findByOrderId_existente_retornaOptional() {
            User user = createUser();
            Order order = createOrder(user);
            em.persistAndFlush(buildInvoice(order, user, "INV-2024-000010"));

            Optional<Invoice> result = invoiceRepository.findByOrderId(order.getId());

            assertTrue(result.isPresent());
            assertEquals("INV-2024-000010", result.get().getInvoiceNumber());
        }

        @Test
        @DisplayName("order sin invoice → retorna Optional vacío")
        void findByOrderId_sinInvoice_retornaVacio() {
            User user = createUser();
            Order order = createOrder(user);

            Optional<Invoice> result = invoiceRepository.findByOrderId(order.getId());
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // findAllByUserId
    // =========================================================================

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserIdTests {

        @Test
        @DisplayName("usuario con varias facturas → devuelve la lista completa")
        void findAllByUserId_conFacturas_devuelveLista() {
            User user = createUser();
            Order order1 = createOrder(user);
            Order order2 = createOrder(user);
            em.persistAndFlush(buildInvoice(order1, user, "INV-2024-000020"));
            em.persistAndFlush(buildInvoice(order2, user, "INV-2024-000021"));

            List<Invoice> result = invoiceRepository.findAllByUserId(user.getId());
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("usuario sin facturas → devuelve lista vacía")
        void findAllByUserId_sinFacturas_devuelveListaVacia() {
            User user = createUser();
            List<Invoice> result = invoiceRepository.findAllByUserId(user.getId());
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User createUser() {
        long ts = System.nanoTime();
        Role role = em.persistAndFlush(Role.builder()
                .name("ROLE_INV_" + ts)
                .createdAt(LocalDateTime.now())
                .build());
        return em.persistAndFlush(User.builder()
                .name("Test")
                .surname("User")
                .username("testuser_inv_" + ts)
                .email("testinv_" + ts + "@test.com")
                .password("encoded")
                .phone("600000000")
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Order createOrder(User user) {
        return em.persistAndFlush(Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(80.0)
                .paymentMethod("STRIPE")
                .discountAmount(0.0)
                .build());
    }

    private Invoice buildInvoice(Order order, User user, String invoiceNumber) {
        return Invoice.builder()
                .order(order)
                .user(user)
                .invoiceNumber(invoiceNumber)
                .status(InvoiceStatus.GENERATED)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(80.0))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
