package es.marcha.backend.core.auth.application.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import es.marcha.backend.core.auth.application.dto.request.LoginRequestDTO;
import es.marcha.backend.core.auth.application.dto.request.RegisterRequestDTO;
import es.marcha.backend.core.error.exception.UserException;
import es.marcha.backend.core.user.application.dto.response.UserResponseDTO;
import es.marcha.backend.core.user.application.service.RoleService;
import es.marcha.backend.core.user.application.service.UserService;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.modules.notification.application.service.UserEmailNotificationService;

/**
 * Tests unitarios para AuthService.
 *
 * Verifica los flujos de login, registro y logout:
 * - Credenciales inválidas lanzan UserException con el código correcto
 * - Datos duplicados/inválidos en registro lanzan UserException
 * - Logout de sesión ya cerrada lanza UserException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — flujos de autenticación")
class AuthServiceTest {

    @Mock
    private UserService uService;

    @Mock
    private RoleService rService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserEmailNotificationService emailService;

    @Mock
    private TokenInvalidationService tokenInvalidationService;

    @InjectMocks
    private AuthService authService;

    // =========================================================================
    // Login
    // =========================================================================

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("contraseña incorrecta → lanza UserException(FAILED_LOGIN)")
        void login_contraseñaIncorrecta_lanzaFAILED_LOGIN() {
            User user = buildActiveUser();
            when(uService.getUserByUsernameOrEmail("testuser")).thenReturn(user);
            when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

            LoginRequestDTO creds = new LoginRequestDTO();
            creds.setUsernameOrEmail("testuser");
            creds.setPassword("wrong");

            UserException ex = assertThrows(UserException.class, () -> authService.login(creds));
            assertEquals(UserException.FAILED_LOGIN, ex.getMessage());
        }

        @Test
        @DisplayName("usuario no encontrado → propaga la excepción de UserService")
        void login_usuarioNoEncontrado_propagaException() {
            when(uService.getUserByUsernameOrEmail("noexiste"))
                    .thenThrow(new UserException(UserException.DEFAULT));

            LoginRequestDTO creds = new LoginRequestDTO();
            creds.setUsernameOrEmail("noexiste");
            creds.setPassword("any");

            assertThrows(UserException.class, () -> authService.login(creds));
        }
    }

    // =========================================================================
    // Registro
    // =========================================================================

    @Nested
    @DisplayName("Registro")
    class RegisterTests {

        @Test
        @DisplayName("username ya existe → lanza UserException(FAILED_CREATE_USER)")
        void register_usernameExistente_lanzaCONFLICT() {
            when(uService.getUserByUsername("existente"))
                    .thenReturn(Optional.of(UserResponseDTO.builder().id(1L).build()));

            RegisterRequestDTO dto = buildRegisterRequest("existente", "a@a.com");

            UserException ex = assertThrows(UserException.class,
                    () -> authService.register(dto));
            assertEquals(UserException.FAILED_CREATE_USER, ex.getMessage());
        }

        @Test
        @DisplayName("email inválido → lanza UserException(FAILED_REGISTER)")
        void register_emailInvalido_lanzaFAILED_REGISTER() {
            when(uService.getUserByUsername("newuser")).thenReturn(Optional.empty());

            RegisterRequestDTO dto = buildRegisterRequest("newuser", "not-an-email");

            UserException ex = assertThrows(UserException.class,
                    () -> authService.register(dto));
            assertEquals(UserException.FAILED_REGISTER, ex.getMessage());
        }

        @Test
        @DisplayName("términos no aceptados → lanza UserException(TERMS_NOT_ACCEPTED)")
        void register_terminosNoAceptados_lanzaTERMS_NOT_ACCEPTED() {
            when(uService.getUserByUsername("newuser")).thenReturn(Optional.empty());

            RegisterRequestDTO dto = buildRegisterRequest("newuser", "valid@email.com");
            dto.setTermsAccepted(false);

            UserException ex = assertThrows(UserException.class,
                    () -> authService.register(dto));
            assertEquals(UserException.TERMS_NOT_ACCEPTED, ex.getMessage());
        }
    }

    // =========================================================================
    // Logout
    // =========================================================================

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("sesión ya cerrada → lanza UserException(USER_LOGGEDOUT)")
        void logout_sesionYaCerrada_lanzaUSER_LOGGEDOUT() {
            User user = buildActiveUser();
            user.setActive(false);
            when(uService.getUserByIdForHandler(1L)).thenReturn(user);

            UserException ex = assertThrows(UserException.class, () -> authService.logout(1L));
            assertEquals(UserException.USER_LOGGEDOUT, ex.getMessage());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User buildActiveUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setActive(true);
        user.setBanned(false);
        user.setDeleted(false);
        user.setSessionCount(0);
        return user;
    }

    private RegisterRequestDTO buildRegisterRequest(String username, String email) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword("password123");
        dto.setName("Test");
        dto.setSurname("User");
        dto.setPhone("600000000");
        dto.setTermsAccepted(true);
        return dto;
    }
}
