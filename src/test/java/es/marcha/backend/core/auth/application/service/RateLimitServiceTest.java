package es.marcha.backend.core.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import es.marcha.backend.core.error.exception.RateLimitException;

/**
 * Tests unitarios para RateLimitService.
 *
 * Verifica el comportamiento de los buckets por tipo de endpoint:
 * - Primera llamada no lanza excepción
 * - Superar el límite lanza RateLimitException con retryAfterSeconds > 0
 * - resetCounter permite reiniciar el bucketq
 *
 * Límites configurados:
 * LOGIN: 5 / 15 min | PASSWORD_RESET: 3 / 1h | DATA_EXPORT: 1 / 1 día
 */
@DisplayName("RateLimitService — gestión de buckets por IP y endpoint")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    // =========================================================================
    // Dentro del límite
    // =========================================================================

    @Nested
    @DisplayName("Dentro del límite")
    class DentroDelLimite {

        @Test
        @DisplayName("primera llamada LOGIN no lanza excepción")
        void checkRateLimit_primeraLlamadaLogin_noLanzaExcepcion() {
            assertDoesNotThrow(
                    () -> rateLimitService.checkRateLimit("192.168.1.1", RateLimitService.EndpointType.LOGIN));
        }

        @Test
        @DisplayName("primera llamada PASSWORD_RESET no lanza excepción")
        void checkRateLimit_primeraLlamadaPasswordReset_noLanzaExcepcion() {
            assertDoesNotThrow(
                    () -> rateLimitService.checkRateLimit("10.0.0.1", RateLimitService.EndpointType.PASSWORD_RESET));
        }

        @Test
        @DisplayName("primera llamada DATA_EXPORT no lanza excepción")
        void checkRateLimit_primeraLlamadaDataExport_noLanzaExcepcion() {
            assertDoesNotThrow(
                    () -> rateLimitService.checkRateLimit("10.0.0.2", RateLimitService.EndpointType.DATA_EXPORT));
        }
    }

    // =========================================================================
    // Superando el límite
    // =========================================================================

    @Nested
    @DisplayName("Superando el límite")
    class SuperandoElLimite {

        @Test
        @DisplayName("6 intentos de LOGIN → sexto lanza RateLimitException")
        void checkRateLimit_login6Intentos_lanzaException() {
            String ip = "192.168.99.1";
            // Los primeros 5 no deben lanzar excepción (límite LOGIN = 5)
            for (int i = 0; i < 5; i++) {
                assertDoesNotThrow(() -> rateLimitService.checkRateLimit(ip,
                        RateLimitService.EndpointType.LOGIN));
            }
            // El sexto debe lanzar RateLimitException
            RateLimitException ex = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkRateLimit(ip, RateLimitService.EndpointType.LOGIN));
            assertTrue(ex.getRetryAfterSeconds() > 0);
        }

        @Test
        @DisplayName("4 intentos de PASSWORD_RESET → cuarto lanza RateLimitException")
        void checkRateLimit_passwordReset4Intentos_lanzaException() {
            String ip = "192.168.99.2";
            // Los primeros 3 no deben lanzar excepción (límite PASSWORD_RESET = 3)
            for (int i = 0; i < 3; i++) {
                assertDoesNotThrow(() -> rateLimitService.checkRateLimit(ip,
                        RateLimitService.EndpointType.PASSWORD_RESET));
            }
            // El cuarto debe lanzar RateLimitException
            RateLimitException ex = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkRateLimit(ip, RateLimitService.EndpointType.PASSWORD_RESET));
            assertTrue(ex.getRetryAfterSeconds() > 0);
        }

        @Test
        @DisplayName("2 intentos de DATA_EXPORT → segundo lanza RateLimitException")
        void checkRateLimit_dataExport2Intentos_lanzaException() {
            String ip = "192.168.99.3";
            // El primero no lanza excepción (límite DATA_EXPORT = 1)
            assertDoesNotThrow(() -> rateLimitService.checkRateLimit(ip,
                    RateLimitService.EndpointType.DATA_EXPORT));
            // El segundo debe lanzar RateLimitException
            assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkRateLimit(ip, RateLimitService.EndpointType.DATA_EXPORT));
        }
    }

    // =========================================================================
    // Reset del contador
    // =========================================================================

    @Nested
    @DisplayName("resetCounter")
    class ResetCounterTests {

        @Test
        @DisplayName("resetCounter permite llamadas al agotarse el bucket")
        void resetCounter_trasSuperarLimite_permiteNuevasLlamadas() {
            String ip = "192.168.99.4";
            // Agotar el bucket DATA_EXPORT (límite = 1)
            assertDoesNotThrow(() -> rateLimitService.checkRateLimit(ip,
                    RateLimitService.EndpointType.DATA_EXPORT));
            assertThrows(RateLimitException.class, () -> rateLimitService.checkRateLimit(ip,
                    RateLimitService.EndpointType.DATA_EXPORT));

            // Resetear el contador
            rateLimitService.resetCounter(ip, RateLimitService.EndpointType.DATA_EXPORT);

            // Ahora debe pasar de nuevo
            assertDoesNotThrow(() -> rateLimitService.checkRateLimit(ip,
                    RateLimitService.EndpointType.DATA_EXPORT));
        }
    }
}
