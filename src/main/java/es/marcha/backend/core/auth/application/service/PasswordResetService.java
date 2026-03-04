package es.marcha.backend.core.auth.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.marcha.backend.core.error.exception.UserException;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.services.mail.UserEmailNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PasswordResetService {

    private final UserRepository uRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailNotificationService userEmailNotificationService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Inicia el flujo de restablecimiento de contraseña para el email indicado.
     *
     * Genera un token UUID con una validez de 1 hora, lo persiste en el usuario
     * y envía el email con el enlace de restablecimiento usando la plantilla
     * {@code emails/user/password-reset}.
     *
     * Si no existe ningún usuario con ese email, el método termina silenciosamente
     * para no revelar qué cuentas existen en el sistema.
     *
     * @param email dirección de correo del usuario que solicita el reseteo
     * @throws IOException
     */
    @Transactional
    public void requestReset(String email) {
        User user = uRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        uRepository.save(user);

        userEmailNotificationService.sendPasswordResetEmail(user.getName(), user.getEmail(), token);
    }

    /**
     * Confirma el restablecimiento de contraseña usando el token recibido por
     * email.
     *
     * Valida que el token exista y no haya expirado, actualiza la contraseña
     * codificada, limpia el token y envía un email de notificación de cambio
     * usando la plantilla {@code emails/user/password-change-notification}.
     *
     * @param token       token UUID recibido en el email de restablecimiento
     * @param newPassword nueva contraseña en texto plano (se codificará con BCrypt)
     * @throws IOException
     * @throws UserException si el token no existe ({@code INVALID_RESET_TOKEN})
     *                       o ha expirado ({@code RESET_TOKEN_EXPIRED})
     */
    @Transactional
    public void confirmReset(String token, String newPassword) {
        User user = uRepository.findByResetToken(token)
                .orElseThrow(() -> new UserException(UserException.INVALID_RESET_TOKEN));

        if (user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UserException(UserException.RESET_TOKEN_EXPIRED);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        uRepository.save(user);

        userEmailNotificationService.sendPasswordChangeNotification(user.getName(), user.getEmail());
    }
}
