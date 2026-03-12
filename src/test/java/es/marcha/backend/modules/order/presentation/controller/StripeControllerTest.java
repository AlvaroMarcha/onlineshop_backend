package es.marcha.backend.modules.order.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.error.exception.StripePaymentException;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.modules.order.application.service.StripeService;

/**
 * Tests de capa web para StripeController.
 *
 * CRÍTICO: POST /stripe/webhook es el único endpoint público fuera de /auth/**
 * e /images/**. La firma del payload debe ser verificada siempre.
 *
 * Verifica:
 * - Firma inválida → 400 BAD REQUEST (seguridad ante peticiones fraudulentas)
 * - Firma válida → 200 OK con mensaje de confirmación
 * - POST /stripe/payment-intent → 201 CREATED al crear PaymentIntent
 */
@WebMvcTest(value = StripeController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("StripeController — webhooks y pagos")
class StripeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeService stripeService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // POST /stripe/webhook — endpoint público crítico
    // =========================================================================

    @Nested
    @DisplayName("POST /stripe/webhook")
    class WebhookTests {

        @Test
        @DisplayName("firma inválida → 400 BAD REQUEST")
        void webhook_firmaInvalida_devuelve400() throws Exception {
            doThrow(new StripePaymentException(StripePaymentException.INVALID_WEBHOOK_SIGNATURE))
                    .when(stripeService).handleWebhookEvent(anyString(), anyString());

            mockMvc.perform(post("/stripe/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=fake,v1=invalid")
                    .content("{\"type\":\"payment_intent.succeeded\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("firma válida → 200 OK")
        void webhook_firmaValida_devuelve200() throws Exception {
            doNothing().when(stripeService).handleWebhookEvent(anyString(), anyString());

            mockMvc.perform(post("/stripe/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=123,v1=valid_sig")
                    .content("{\"type\":\"payment_intent.succeeded\"}"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // POST /stripe/payment-intent
    // =========================================================================

    @Nested
    @DisplayName("POST /stripe/payment-intent")
    class PaymentIntentTests {

        @Test
        @DisplayName("crea PaymentIntent → 201 CREATED")
        void createPaymentIntent_peticionValida_devuelve201() throws Exception {
            es.marcha.backend.modules.order.application.dto.response.StripePaymentIntentResponseDTO response = new es.marcha.backend.modules.order.application.dto.response.StripePaymentIntentResponseDTO();
            when(stripeService.createPaymentIntent(anyLong(), anyString())).thenReturn(response);

            mockMvc.perform(post("/stripe/payment-intent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderId\": 1, \"currency\": \"eur\"}"))
                    .andExpect(status().isCreated());
        }
    }
}
