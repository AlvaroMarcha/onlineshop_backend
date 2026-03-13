package es.marcha.backend.core.user.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

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

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.core.user.application.dto.response.AddressResponseDTO;
import es.marcha.backend.core.user.application.service.AddressService;
import es.marcha.backend.core.user.domain.model.Address;

/**
 * Tests de capa web para AddressController.
 *
 * Verifica:
 * - GET /address/{userId} → 200 OK con lista de direcciones
 * - POST /address → 200 OK con dirección creada
 * - PUT /address → 200 OK con dirección actualizada
 */
@WebMvcTest(value = AddressController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("AddressController — gestión de direcciones de usuario")
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService aService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /address/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /address/{id}")
    class GetAddressesByUserTests {

        @Test
        @DisplayName("devuelve 200 con lista de direcciones del usuario")
        void getAllAddressByUser_devuelve200() throws Exception {
            AddressResponseDTO dto = new AddressResponseDTO();
            when(aService.getAllAddressesByUserId(1L)).thenReturn(List.of(dto));

            mockMvc.perform(get("/address/1").with(user("testuser")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("usuario sin direcciones → 200 con lista vacía")
        void getAllAddressByUser_sinDirecciones_devuelve200() throws Exception {
            when(aService.getAllAddressesByUserId(99L)).thenReturn(List.of());

            mockMvc.perform(get("/address/99").with(user("testuser")))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // POST /address
    // =========================================================================

    @Nested
    @DisplayName("POST /address")
    class SaveAddressTests {

        @Test
        @DisplayName("guarda dirección → 200 OK")
        void saveAddress_peticionValida_devuelve200() throws Exception {
            AddressResponseDTO response = new AddressResponseDTO();
            when(aService.saveAddress(any(Address.class))).thenReturn(response);

            String body = """
                    {
                        "street": "Calle Mayor 1",
                        "city": "Madrid",
                        "postalCode": "28001",
                        "country": "España"
                    }
                    """;

            mockMvc.perform(post("/address")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(user("testuser")))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // PUT /address
    // =========================================================================

    @Nested
    @DisplayName("PUT /address")
    class UpdateAddressTests {

        @Test
        @DisplayName("actualiza dirección → 200 OK")
        void updateAddress_peticionValida_devuelve200() throws Exception {
            AddressResponseDTO response = new AddressResponseDTO();
            when(aService.updateAddress(any(Address.class))).thenReturn(response);

            String body = """
                    {
                        "id": 1,
                        "street": "Calle Nueva 5",
                        "city": "Barcelona",
                        "postalCode": "08001",
                        "country": "España"
                    }
                    """;

            mockMvc.perform(put("/address")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(user("testuser")))
                    .andExpect(status().isOk());
        }
    }
}
