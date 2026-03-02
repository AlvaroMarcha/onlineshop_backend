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
import es.marcha.backend.services.security.RateLimitService;
import es.marcha.backend.services.security.RateLimitService.EndpointType;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService aService;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private RateLimitService rateLimitService;

    /**
     * Autentica un usuario mediante sus credenciales (username o email y
     * contraseña).
     *
     * <p>
     * Rate limit: máximo 5 intentos por IP cada 15 minutos.
     * Los intentos exitosos resetean el contador.
     * </p>
     *
     * @param credentials DTO con el username/email y la contraseña del usuario.
     * @param request     Petición HTTP para obtener la IP del cliente.
     * @return {@link ResponseEntity} con el {@link AuthResponseDTO} que incluye
     *         el usuario autenticado y el token JWT generado, con código HTTP 200
     *         OK.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO credentials,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        rateLimitService.checkRateLimit(ip, EndpointType.LOGIN);
        AuthResponseDTO response = aService.login(credentials);
        rateLimitService.resetCounter(ip, EndpointType.LOGIN);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>
     * Rate limit: máximo 10 peticiones por IP cada hora.
     * Los registros exitosos resetean el contador.
     * </p>
     *
     * @param userData DTO con los datos necesarios para el registro (nombre,
     *                 apellido, username, email, contraseña y teléfono).
     * @param request  Petición HTTP para obtener la IP del cliente.
     * @return {@link ResponseEntity} con el {@link AuthResponseDTO} que incluye
     *         el usuario registrado y el token JWT generado, con código HTTP 200
     *         OK.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO userData,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        rateLimitService.checkRateLimit(ip, EndpointType.REGISTER);
        AuthResponseDTO response = aService.register(userData);
        rateLimitService.resetCounter(ip, EndpointType.REGISTER);

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
     * <p>
     * Genera un token con validez de 1 hora y envía un email con el enlace
     * de restablecimiento. Si el email no existe, responde igualmente con 200
     * para no revelar qué cuentas están registradas.
     * </p>
     *
     * <p>
     * Rate limit: máximo 3 intentos por IP cada hora.
     * Las solicitudes exitosas resetean el contador.
     * </p>
     *
     * @param body    DTO con el email del usuario.
     * @param request Petición HTTP para obtener la IP del cliente.
     * @return {@link ResponseEntity} con código HTTP 200 OK.
     * @throws IOException
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @RequestBody PasswordResetRequestDTO body,
            HttpServletRequest request) throws IOException {

        String ip = getClientIp(request);
        rateLimitService.checkRateLimit(ip, EndpointType.PASSWORD_RESET);
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
     * @throws es.marcha.backend.exception.UserException con código
     *                                                   {@code INVALID_RESET_TOKEN}
     *                                                   si el token no existe,
     *                                                   o
     *                                                   {@code RESET_TOKEN_EXPIRED}
     *                                                   si ha caducado.
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody PasswordResetConfirmDTO body) throws IOException {
        passwordResetService.confirmReset(body.getToken(), body.getNewPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene la IP real del cliente teniendo en cuenta proxies/balanceadores.
     * Prioriza el header {@code X-Forwarded-For}; si no está presente, usa la IP
     * directa de la conexión.
     *
     * @param request la petición HTTP entrante
     * @return la IP del cliente como cadena de texto
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
