package es.marcha.backend.core.user.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.UserException;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.modules.notification.application.service.UserEmailNotificationService;

/**
 * Tests unitarios para UserDeletionService.
 *
 * Regla RGPD: {@code DELETE /users/me} ANONIMIZA la cuenta, nunca la borra.
 * Los pedidos vinculados se conservan de forma anónima 10 años.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDeletionService — anonimización RGPD")
class UserDeletionServiceTest {

    @Mock
    private UserRepository uRepository;

    @Mock
    private UserEmailNotificationService userEmailNotificationService;

    @InjectMocks
    private UserDeletionService userDeletionService;

    // =========================================================================
    // anonymizeAndDelete
    // =========================================================================

    @Nested
    @DisplayName("anonymizeAndDelete")
    class AnonymizeAndDeleteTests {

        @Test
        @DisplayName("usuario encontrado → campos anonimizados y save() llamado, delete() nunca")
        void anonymizeAndDelete_usuarioEncontrado_anonimizaYNoElimina() {
            User user = buildUser();
            when(uRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(uRepository.save(any(User.class))).thenReturn(user);

            userDeletionService.anonymizeAndDelete("testuser");

            // Debe llamar a save, NUNCA a delete
            verify(uRepository).save(user);
            verify(uRepository, never()).delete(any(User.class));
            verify(uRepository, never()).deleteById(anyLong());

            // Datos personales anonimizados
            assertEquals("Usuario", user.getName());
            assertEquals("Eliminado", user.getSurname());
            assertTrue(user.getUsername().startsWith("deleted_"));
            assertTrue(user.getEmail().contains("@eliminado.local"));
            assertNull(user.getProfileImageUrl());
            assertNull(user.getResetToken());

            // Estado lógico
            assertFalse(user.isActive());
            assertTrue(user.isDeleted());
        }

        @Test
        @DisplayName("email de notificación enviado ANTES de anonimizar")
        void anonymizeAndDelete_enviaEmailConDatosRealesAntesDeAnonimizar() {
            User user = buildUser();
            String realName = user.getName();
            String realEmail = user.getEmail();

            when(uRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(uRepository.save(any())).thenReturn(user);

            userDeletionService.anonymizeAndDelete("testuser");

            // El email debe haberse enviado con los datos reales (antes de anonimizar)
            verify(userEmailNotificationService).sendAccountDeletionEmail(
                    eq(realName), eq(realEmail), anyString(), eq(user.getId()));
        }

        @Test
        @DisplayName("usuario no encontrado → lanza UserException")
        void anonymizeAndDelete_usuarioNoEncontrado_lanzaUserException() {
            when(uRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

            assertThrows(UserException.class,
                    () -> userDeletionService.anonymizeAndDelete("noexiste"));
            verify(uRepository, never()).save(any());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User buildUser() {
        User user = new User();
        user.setId(42L);
        user.setName("Juan");
        user.setSurname("García");
        user.setUsername("testuser");
        user.setEmail("juan@test.com");
        user.setPhone("600000000");
        user.setActive(true);
        user.setDeleted(false);
        return user;
    }
}
