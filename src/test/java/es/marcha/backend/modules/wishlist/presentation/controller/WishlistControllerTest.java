package es.marcha.backend.modules.wishlist.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import es.marcha.backend.modules.wishlist.application.dto.request.AddWishlistItemRequestDTO;
import es.marcha.backend.modules.wishlist.application.dto.response.WishlistResponseDTO;
import es.marcha.backend.modules.wishlist.application.service.WishlistService;

/**
 * Tests de capa web para WishlistController.
 *
 * Verifica:
 * - GET /wishlist devuelve 200 con la wishlist
 * - POST /wishlist/items devuelve 201 al agregar ítem
 * - DELETE /wishlist/items/{id} devuelve 200 al eliminar ítem
 */
@WebMvcTest(value = WishlistController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("WishlistController — endpoints de la wishlist")
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /wishlist
    // =========================================================================

    @Nested
    @DisplayName("GET /wishlist")
    class GetWishlistTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("devuelve 200 con la wishlist del usuario")
        void getWishlist_autenticado_devuelve200() throws Exception {
            WishlistResponseDTO wishlist = new WishlistResponseDTO();
            when(wishlistService.getWishlist("testuser")).thenReturn(wishlist);

            mockMvc.perform(get("/wishlist").with(user("testuser")))
                    .andExpect(status().isOk());

            verify(wishlistService).getWishlist("testuser");
        }
    }

    // =========================================================================
    // POST /wishlist/items
    // =========================================================================

    @Nested
    @DisplayName("POST /wishlist/items")
    class AddItemTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("añade producto a wishlist → devuelve 201")
        void addItem_productoValido_devuelve201() throws Exception {
            AddWishlistItemRequestDTO request = new AddWishlistItemRequestDTO();
            request.setProductId(1L);

            WishlistResponseDTO wishlist = new WishlistResponseDTO();
            when(wishlistService.addItem(eq("testuser"), eq(1L))).thenReturn(wishlist);

            mockMvc.perform(post("/wishlist/items")
                    .with(user("testuser"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // DELETE /wishlist/items/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /wishlist/items/{id}")
    class RemoveItemTests {

        @Test        @WithMockUser(username = "testuser")        @DisplayName("elimina ítem de la wishlist → devuelve 200")
        void removeItem_autenticado_devuelve200() throws Exception {
            WishlistResponseDTO wishlist = new WishlistResponseDTO();
            when(wishlistService.removeItem(eq("testuser"), eq(5L))).thenReturn(wishlist);

            mockMvc.perform(delete("/wishlist/items/5").with(user("testuser")))
                    .andExpect(status().isOk());
        }
    }
}
