package es.marcha.backend.modules.cart.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
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
import es.marcha.backend.modules.cart.application.dto.request.AddCartItemRequestDTO;
import es.marcha.backend.modules.cart.application.dto.response.CartResponseDTO;
import es.marcha.backend.modules.cart.application.service.CartService;

/**
 * Tests de capa web para CartController.
 *
 * Verifica:
 * - GET /cart devuelve 200 con el carrito
 * - POST /cart/items devuelve 201 al agregar ítem
 * - DELETE /cart devuelve 204 al vaciar el carrito
 */
@WebMvcTest(value = CartController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("CartController — endpoints del carrito")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /cart
    // =========================================================================

    @Nested
    @DisplayName("GET /cart")
    class GetCartTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("devuelve 200 con el carrito del usuario autenticado")
        void getCart_autenticado_devuelve200() throws Exception {
            CartResponseDTO cart = new CartResponseDTO();
            when(cartService.getCartByUsername("testuser")).thenReturn(cart);

            mockMvc.perform(get("/cart"))
                    .andExpect(status().isOk());

            verify(cartService).getCartByUsername("testuser");
        }
    }

    // =========================================================================
    // POST /cart/items
    // =========================================================================

    @Nested
    @DisplayName("POST /cart/items")
    class AddItemTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("añade ítem válido → devuelve 201")
        void addItem_peticionValida_devuelve201() throws Exception {
            AddCartItemRequestDTO request = new AddCartItemRequestDTO();
            request.setProductId(1L);
            request.setQuantity(2);

            CartResponseDTO cart = new CartResponseDTO();
            when(cartService.addItem(eq("testuser"), any(AddCartItemRequestDTO.class)))
                    .thenReturn(cart);

            mockMvc.perform(post("/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // DELETE /cart
    // =========================================================================

    @Nested
    @DisplayName("DELETE /cart")
    class ClearCartTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("vaciado del carrito → devuelve 204")
        void clearCart_autenticado_devuelve204() throws Exception {
            doNothing().when(cartService).clearCartByUsername("testuser");

            mockMvc.perform(delete("/cart"))
                    .andExpect(status().isNoContent());
        }
    }
}
