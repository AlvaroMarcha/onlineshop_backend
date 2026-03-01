package es.marcha.backend.services.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.UserRepository;
import es.marcha.backend.services.mail.MailService;
import es.marcha.backend.services.media.MediaService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PasswordResetService {

    private final UserRepository uRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final MediaService mService;

    @Value("${app.frontend-url:http://localhost:4200}")
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
    public void requestReset(String email) throws IOException {
        User user = uRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        uRepository.save(user);

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

        Context ctx = new Context();
        ctx.setVariable("userName", user.getName());
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("hasLogo", logo.isPresent());

        String html = templateEngine.process("emails/user/password-reset", ctx);

        try {
            mailService.sendHtmlEmailWithInline(user.getEmail(), "Restablece tu contraseña", html, logo);
        } catch (MessagingException e) {
            log.error("Error sending password reset email to {}: {}", email, e.getMessage());
        }
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
    public void confirmReset(String token, String newPassword) throws IOException {
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

        String resetLink = frontendUrl + "/reset-password";

        Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

        Context ctx = new Context();
        ctx.setVariable("userName", user.getName());
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("hasLogo", logo.isPresent());

        String html = templateEngine.process("emails/user/password-change-notification", ctx);

        try {
            mailService.sendHtmlEmailWithInline(user.getEmail(), "Tu contraseña ha sido cambiada", html, logo);
        } catch (MessagingException e) {
            log.error("Error sending password change notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
