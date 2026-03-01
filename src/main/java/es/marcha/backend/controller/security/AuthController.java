package es.marcha.backend.controller.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.request.security.LoginRequestDTO;
import es.marcha.backend.dto.request.security.LogoutRequestDTO;
import es.marcha.backend.dto.request.security.PasswordResetConfirmDTO;
import es.marcha.backend.dto.request.security.PasswordResetRequestDTO;
import es.marcha.backend.dto.request.security.RegisterRequestDTO;
import es.marcha.backend.dto.response.security.AuthResponseDTO;
import es.marcha.backend.dto.response.user.LogoutResponseDTO;
import es.marcha.backend.services.security.AuthService;
import es.marcha.backend.services.security.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // Attribs
    @Autowired
    private AuthService aService;
    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Autentica un usuario mediante sus credenciales (username o email y
     * contraseña).
     *
     * @param credentials DTO con el username/email y la contraseña del usuario.
     * @return {@link ResponseEntity} con el {@link AuthResponseDTO} que incluye
     *         el usuario autenticado y el token JWT generado, con código HTTP 200
     *         OK.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO credentials) {
        AuthResponseDTO response = aService.login(credentials);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param userData DTO con los datos necesarios para el registro (nombre,
     *                 apellido,
     *                 username, email, contraseña y teléfono).
     * @return {@link ResponseEntity} con el {@link AuthResponseDTO} que incluye
     *         el usuario registrado y el token JWT generado, con código HTTP 200
     *         OK.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO userData) {
        AuthResponseDTO response = aService.register(userData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cierra la sesión del usuario indicado, marcándolo como inactivo.
     *
     * @param data DTO que contiene el ID del usuario cuya sesión se desea cerrar.
     * @return {@link ResponseEntity} con el {@link LogoutResponseDTO} que confirma
     *         el cierre de sesión, con código HTTP 200 OK.
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO data) {
        LogoutResponseDTO msg = aService.logout(data.getUserId());
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

    /**
     * Inicia el flujo de restablecimiento de contraseña.
     *
     * Genera un token con validez de 1 hora y envía un email con el enlace
     * de restablecimiento. Si el email no existe, responde igualmente con 200
     * para no revelar qué cuentas están registradas.
     *
     * @param body DTO con el email del usuario.
     * @return {@link ResponseEntity} con código HTTP 200 OK.
     * @throws IOException
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody PasswordResetRequestDTO body) throws IOException {
        passwordResetService.requestReset(body.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Confirma el restablecimiento de contraseña con el token recibido por email.
     *
     * Valida el token, actualiza la contraseña y envía un email de notificación
     * de cambio.
     *
     * @param body DTO con el token y la nueva contraseña.
     * @return {@link ResponseEntity} con código HTTP 200 OK.
     * @throws IOException
     * @throws UserException con código {@code INVALID_RESET_TOKEN} si el token no
     *                       existe,
     *                       o {@code RESET_TOKEN_EXPIRED} si ha caducado.
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody PasswordResetConfirmDTO body) throws IOException {
        passwordResetService.confirmReset(body.getToken(), body.getNewPassword());
        return ResponseEntity.ok().build();
    }

}
