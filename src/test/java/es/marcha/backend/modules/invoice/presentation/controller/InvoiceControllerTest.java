package es.marcha.backend.modules.invoice.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.modules.invoice.application.service.InvoiceService;
import es.marcha.backend.modules.invoice.application.service.InvoiceService.InvoiceGenerationResult;
import es.marcha.backend.modules.invoice.domain.model.Invoice;

/**
 * Tests de capa web para InvoiceController.
 *
 * Regla de negocio: endpoint idempotente — llamar dos veces para el mismo
 * pedido devuelve la misma factura sin regenerarla.
 * Numeración: INV-YYYY-NNNNNN con bloqueo PESSIMISTIC_WRITE.
 *
 * Verifica:
 * - POST /invoices/orders/{orderId} → 201 CREATED cuando la factura es nueva
 * - POST /invoices/orders/{orderId} → 200 OK cuando la factura ya existe
 * - GET /invoices/users/{userId} → 200 OK
 * - Idempotencia: segunda llamada devuelve 200 OK con la misma factura
 */
@WebMvcTest(value = InvoiceController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("InvoiceController — generación y consulta de facturas")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // POST /invoices/orders/{orderId}
    // =========================================================================

    @Nested
    @DisplayName("POST /invoices/orders/{orderId}")
    class GenerateInvoiceTests {

        @Test
        @DisplayName("genera factura → 201 CREATED")
        void generateInvoice_ordenValida_devuelve201() throws Exception {
            Invoice invoice = buildInvoice("INV-2024-000001");
            when(invoiceService.generateInvoice(1L))
                    .thenReturn(new InvoiceGenerationResult(invoice, true));

            mockMvc.perform(post("/invoices/orders/1"))
                    .andExpect(status().isCreated());

            verify(invoiceService).generateInvoice(1L);
        }

        @Test
        @DisplayName("factura ya existe (idempotente) → 200 OK sin duplicar")
        void generateInvoice_yaExiste_devuelve200Idempotente() throws Exception {
            Invoice existingInvoice = buildInvoice("INV-2024-000001");
            when(invoiceService.generateInvoice(1L))
                    .thenReturn(new InvoiceGenerationResult(existingInvoice, false));

            mockMvc.perform(post("/invoices/orders/1"))
                    .andExpect(status().isOk());

            verify(invoiceService).generateInvoice(1L);
        }
    }

    // =========================================================================
    // GET /invoices/users/{userId}
    // =========================================================================

    @Nested
    @DisplayName("GET /invoices/users/{userId}")
    class GetInvoicesByUserTests {

        @Test
        @DisplayName("devuelve 200 con lista de facturas del usuario")
        void getInvoicesByUser_devuelve200() throws Exception {
            when(invoiceService.getInvoicesByUser(1L)).thenReturn(List.of());

            mockMvc.perform(get("/invoices/users/1"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Invoice buildInvoice(String number) {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber(number);
        return invoice;
    }
}
