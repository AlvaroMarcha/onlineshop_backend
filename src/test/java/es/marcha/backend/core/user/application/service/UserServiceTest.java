package es.marcha.backend.core.user.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.auth.application.service.TokenInvalidationService;
import es.marcha.backend.core.error.exception.UserException;
import es.marcha.backend.core.filestorage.application.service.MediaService;
import es.marcha.backend.core.user.application.dto.response.UserResponseDTO;
import es.marcha.backend.core.user.domain.model.Role;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;

/**
 * Tests unitarios para UserService.
 *
 * Verifica:
 * - Obtención de usuario por ID con filtros de estado (eliminado, baneado)
 * - Búsqueda por username retorna Optional vacío si no existe
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — gestión de usuarios")
class UserServiceTest {

    @Mock
    private UserRepository uRepository;

    @Mock
    private RoleService rService;

    @Mock
    private MediaService mService;

    @Mock
    private TokenInvalidationService tokenInvalidationService;

    @InjectMocks
    private UserService userService;

    // =========================================================================
    // getUserById
    // =========================================================================

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("usuario activo y no baneado → devuelve DTO")
        void getUserById_activo_devuelveDTO() {
            User user = buildUser(false, false);
            when(uRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponseDTO result = userService.getUserById(1L);
            assertNotNull(result);
        }

        @Test
        @DisplayName("usuario no encontrado → lanza UserException")
        void getUserById_noEncontrado_lanzaException() {
            when(uRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UserException.class, () -> userService.getUserById(99L));
        }

        @Test
        @DisplayName("usuario eliminado → lanza UserException")
        void getUserById_eliminado_lanzaException() {
            User user = buildUser(true, false); // isDeleted=true
            when(uRepository.findById(2L)).thenReturn(Optional.of(user));

            assertThrows(UserException.class, () -> userService.getUserById(2L));
        }

        @Test
        @DisplayName("usuario baneado → lanza UserException")
        void getUserById_baneado_lanzaException() {
            User user = buildUser(false, true); // isBanned=true
            when(uRepository.findById(3L)).thenReturn(Optional.of(user));

            assertThrows(UserException.class, () -> userService.getUserById(3L));
        }
    }

    // =========================================================================
    // getUserByUsername
    // =========================================================================

    @Nested
    @DisplayName("getUserByUsername")
    class GetUserByUsernameTests {

        @Test
        @DisplayName("username no existe → devuelve Optional vacío")
        void getUserByUsername_noExiste_devuelveOptionalVacio() {
            when(uRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

            Optional<UserResponseDTO> result = userService.getUserByUsername("noexiste");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("username existe → devuelve Optional con DTO")
        void getUserByUsername_existe_devuelveOptional() {
            User user = buildUser(false, false);
            when(uRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

            Optional<UserResponseDTO> result = userService.getUserByUsername("testuser");
            assertTrue(result.isPresent());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User buildUser(boolean deleted, boolean banned) {
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        User user = new User();
        user.setId(1L);
        user.setName("Test");
        user.setSurname("User");
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setPhone("600000000");
        user.setDeleted(deleted);
        user.setBanned(banned);
        user.setActive(true);
        user.setRole(role);
        return user;
    }
}
