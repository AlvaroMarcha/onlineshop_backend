package es.marcha.backend.core.user.presentation.controller;

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
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import es.marcha.backend.core.auth.application.service.RateLimitService;
import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.filestorage.application.service.MediaService;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.core.user.application.dto.response.DataExportResponseDTO;
import es.marcha.backend.core.user.application.service.DataExportService;
import es.marcha.backend.core.user.application.service.UserDeletionService;
import es.marcha.backend.core.user.application.service.UserService;

/**
 * Tests de capa web para UserController.
 *
 * Verifica los endpoints críticos de RGPD:
 * - DELETE /users/me → anonimiza la cuenta y devuelve 200
 * - GET /users/me/data-export → exporta datos y devuelve 200
 *
 * Regla de negocio: DELETE /users/me ANONIMIZA, nunca elimina el registro.
 */
@WebMvcTest(value = UserController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("UserController — RGPD y gestión de cuenta")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService uService;

    @MockBean
    private MediaService mService;

    @MockBean
    private UserDeletionService userDeletionService;

    @MockBean
    private DataExportService dataExportService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // DELETE /users/me — RGPD: anonimización
    // =========================================================================

    @Nested
    @DisplayName("DELETE /users/me — anonimización RGPD")
    class DeleteMyAccountTests {

        @Test
        @DisplayName("usuario autenticado → devuelve 200 y llama a anonymizeAndDelete")
        void deleteMyAccount_autenticado_devuelve200() throws Exception {
            doNothing().when(userDeletionService).anonymizeAndDelete("testuser");

            mockMvc.perform(delete("/users/me").with(user("testuser")))
                    .andExpect(status().isOk());

            verify(userDeletionService).anonymizeAndDelete("testuser");
        }
    }

    // =========================================================================
    // GET /users/me/data-export — RGPD Art. 20
    // =========================================================================

    @Nested
    @DisplayName("GET /users/me/data-export — exportación de datos RGPD")
    class DataExportTests {

        @Test
        @DisplayName("sin rate limit → devuelve 200 con datos exportados")
        void exportMyData_sinRateLimit_devuelve200() throws Exception {
            DataExportResponseDTO export = new DataExportResponseDTO();
            doNothing().when(rateLimitService)
                    .checkRateLimit(eq("testuser"), eq(RateLimitService.EndpointType.DATA_EXPORT));
            when(dataExportService.export("testuser")).thenReturn(export);

            mockMvc.perform(get("/users/me/data-export").with(user("testuser")))
                    .andExpect(status().isOk());
        }
    }
}
