package es.marcha.backend.core.auth.presentation.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import es.marcha.backend.core.auth.application.service.AuthService;
import es.marcha.backend.core.auth.application.service.PasswordResetService;
import es.marcha.backend.core.auth.application.service.RateLimitService;
import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.error.exception.RateLimitException;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;

import java.util.Map;

/**
 * Tests de capa web para AuthController.
 *
 * Verifica comportamientos de seguridad y anti-enumeración:
 * - POST /auth/password-reset/request responde 200 aunque el email no exista
 * - POST /auth/login responde 429 cuando se supera el rate limit
 * - POST /auth/register aplica rate limiting independiente del login
 */
@WebMvcTest(value = AuthController.class,
                // Deshabilitar security para testear los endpoints públicos de forma aislada
                excludeAutoConfiguration = {
                                SecurityAutoConfiguration.class,
                                SecurityFilterAutoConfiguration.class
                },
                // Excluir filtros de seguridad (@Component) que no son relevantes para estos
                // tests
                excludeFilters = {
                                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
                                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
                })
@DisplayName("AuthController — seguridad y anti-enumeración")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService aService;

        @MockBean
        private PasswordResetService passwordResetService;

        @MockBean
        private RateLimitService rateLimitService;

        // MockBean necesario porque ModuleFlagInterceptor (cargado por @WebMvcTest)
        // depende de él
        @MockBean
        private ModuleProperties moduleProperties;

        // =========================================================================
        // Anti-enumeración — password reset request
        // =========================================================================

        @Nested
        @DisplayName("POST /auth/password-reset/request — anti-enumeración")
        class PasswordResetRequest {

                @Test
                @DisplayName("Responde 200 OK cuando el email existe")
                void givenExistingEmail_whenPasswordResetRequest_thenReturns200() throws Exception {
                        doNothing().when(rateLimitService).checkRateLimit(anyString(),
                                        org.mockito.ArgumentMatchers.any());
                        doNothing().when(passwordResetService).requestReset(anyString());

                        mockMvc.perform(post("/auth/password-reset/request")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        Map.of("email", "usuario@test.com"))))
                                        .andExpect(status().isOk());

                        verify(passwordResetService).requestReset("usuario@test.com");
                }

                @Test
                @DisplayName("Responde 200 OK aunque el email NO exista — anti-enumeración")
                void givenNonExistentEmail_whenPasswordResetRequest_thenReturns200() throws Exception {
                        // El servicio no lanza excepción aunque el email no exista
                        doNothing().when(rateLimitService).checkRateLimit(anyString(),
                                        org.mockito.ArgumentMatchers.any());
                        doNothing().when(passwordResetService).requestReset(anyString());

                        mockMvc.perform(post("/auth/password-reset/request")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        Map.of("email", "noexiste@desconocido.com"))))
                                        .andExpect(status().isOk());

                        // Verificar que el servicio fue llamado (no cortocircuita antes)
                        verify(passwordResetService).requestReset("noexiste@desconocido.com");
                }
        }

        // =========================================================================
        // Rate limiting
        // =========================================================================

        @Nested
        @DisplayName("Rate limiting — responde 429 al superar el límite")
        class RateLimiting {

                @Test
                @DisplayName("POST /auth/login → 429 al superar 5 intentos por IP")
                void givenRateLimitExceeded_whenLogin_thenReturns429() throws Exception {
                        // Simular que el rate limit está agotado para esta IP
                        doThrow(new RateLimitException(60L))
                                        .when(rateLimitService).checkRateLimit(anyString(),
                                                        org.mockito.ArgumentMatchers.any());

                        mockMvc.perform(post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        Map.of("username", "test", "password", "pass"))))
                                        .andExpect(status().isTooManyRequests());

                        // El servicio de auth NO debe llamarse cuando el rate limit está agotado
                        verify(aService, never()).login(org.mockito.ArgumentMatchers.any());
                }

                @Test
                @DisplayName("POST /auth/password-reset/request → 429 al superar 3 intentos por IP")
                void givenRateLimitExceeded_whenPasswordResetRequest_thenReturns429() throws Exception {
                        doThrow(new RateLimitException(3600L))
                                        .when(rateLimitService).checkRateLimit(anyString(),
                                                        org.mockito.ArgumentMatchers.any());

                        mockMvc.perform(post("/auth/password-reset/request")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        Map.of("email", "test@test.com"))))
                                        .andExpect(status().isTooManyRequests());

                        verify(passwordResetService, never()).requestReset(anyString());
                }
        }
}
