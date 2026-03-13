package es.marcha.backend.core.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.marcha.backend.core.error.exception.UserException;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.modules.notification.application.service.UserEmailNotificationService;

/**
 * Tests unitarios para PasswordResetService.
 *
 * Verifica el ciclo completo de restablecimiento de contraseña:
 * - Solicitud silenciosa si el email no existe (anti-enumeración)
 * - Generación y envío de token para email válido
 * - Validación del token: inválido o caducado lanza UserException
 * - Confirmación exitosa limpia el token y actualiza la contraseña
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService — ciclo de restablecimiento de contraseña")
class PasswordResetServiceTest {

    @Mock
    private UserRepository uRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEmailNotificationService userEmailNotificationService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    // =========================================================================
    // requestReset
    // =========================================================================

    @Nested
    @DisplayName("requestReset")
    class RequestResetTests {

        @Test
        @DisplayName("email no existe → termina silenciosamente sin lanzar excepción")
        void requestReset_emailNoExiste_terminaSilenciosamente() {
            when(uRepository.findByEmail("noexiste@email.com")).thenReturn(Optional.empty());

            // No debe lanzar excepción (anti-enumeración: siempre 200)
            assertDoesNotThrow(() -> passwordResetService.requestReset("noexiste@email.com"));
            verify(userEmailNotificationService, never()).sendPasswordResetEmail(any(), any(), any());
        }

        @Test
        @DisplayName("email existe → guarda token y envía email")
        void requestReset_emailExiste_guardaTokenYEnviaEmail() {
            User user = buildUser("reset@test.com");
            when(uRepository.findByEmail("reset@test.com")).thenReturn(Optional.of(user));
            when(uRepository.save(any(User.class))).thenReturn(user);

            passwordResetService.requestReset("reset@test.com");

            assertNotNull(user.getResetToken());
            assertNotNull(user.getResetTokenExpiry());
            verify(userEmailNotificationService).sendPasswordResetEmail(
                    eq(user.getName()), eq(user.getEmail()), anyString());
        }
    }

    // =========================================================================
    // confirmReset
    // =========================================================================

    @Nested
    @DisplayName("confirmReset")
    class ConfirmResetTests {

        @Test
        @DisplayName("token inválido → lanza UserException(INVALID_RESET_TOKEN)")
        void confirmReset_tokenInvalido_lanzaINVALID_RESET_TOKEN() {
            when(uRepository.findByResetToken("bad-token")).thenReturn(Optional.empty());

            UserException ex = assertThrows(UserException.class,
                    () -> passwordResetService.confirmReset("bad-token", "newpass"));
            assertEquals(UserException.INVALID_RESET_TOKEN, ex.getMessage());
        }

        @Test
        @DisplayName("token caducado → lanza UserException(RESET_TOKEN_EXPIRED)")
        void confirmReset_tokenCaducado_lanzaRESET_TOKEN_EXPIRED() {
            User user = buildUser("expired@test.com");
            user.setResetToken("expired-token");
            user.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1)); // ya caducó

            when(uRepository.findByResetToken("expired-token")).thenReturn(Optional.of(user));

            UserException ex = assertThrows(UserException.class,
                    () -> passwordResetService.confirmReset("expired-token", "newpass"));
            assertEquals(UserException.RESET_TOKEN_EXPIRED, ex.getMessage());
        }

        @Test
        @DisplayName("token válido → actualiza contraseña y limpia token")
        void confirmReset_tokenValido_actualizaContraseña() {
            User user = buildUser("ok@test.com");
            user.setResetToken("valid-token");
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // vigente

            when(uRepository.findByResetToken("valid-token")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newpass")).thenReturn("encoded-newpass");
            when(uRepository.save(any())).thenReturn(user);

            passwordResetService.confirmReset("valid-token", "newpass");

            assertEquals("encoded-newpass", user.getPassword());
            assertNull(user.getResetToken());
            assertNull(user.getResetTokenExpiry());
            verify(userEmailNotificationService).sendPasswordChangeNotification(
                    eq(user.getName()), eq(user.getEmail()));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User buildUser(String email) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setUsername("testuser");
        return user;
    }
}
