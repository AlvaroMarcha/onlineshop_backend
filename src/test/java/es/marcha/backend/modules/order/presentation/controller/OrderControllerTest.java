package es.marcha.backend.modules.order.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.modules.order.application.dto.request.OrderRequestDTO;
import es.marcha.backend.modules.order.application.dto.response.OrderResponseDTO;
import es.marcha.backend.modules.order.application.service.OrderService;
import es.marcha.backend.modules.order.application.service.PaymentService;

/**
 * Tests de capa web para OrderController.
 *
 * Regla de negocio: totalAmount siempre calculado en backend.
 * Verifica:
 * - GET /orders/users/{id} devuelve 200 con lista de órdenes
 * - POST /orders devuelve 201 al crear una nueva orden
 */
@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("OrderController — gestión de pedidos")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService oService;

    @MockBean
    private PaymentService pService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /orders/users/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /orders/users/{id}")
    class GetOrdersTests {

        @Test
        @DisplayName("devuelve 200 con lista de órdenes del usuario")
        void getOrdersByUser_devuelve200() throws Exception {
            when(oService.getAllOrders(1L)).thenReturn(List.of());

            mockMvc.perform(get("/orders/users/1").with(user("testuser")))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // POST /orders
    // =========================================================================

    @Nested
    @DisplayName("POST /orders")
    class CreateOrderTests {

        @Test
        @DisplayName("orden válida → devuelve 201")
        void createOrder_peticionValida_devuelve201() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setUserId(1L);
            request.setItems(List.of());

            OrderResponseDTO response = new OrderResponseDTO();
            when(oService.saveNewOrder(any(OrderRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }
}
