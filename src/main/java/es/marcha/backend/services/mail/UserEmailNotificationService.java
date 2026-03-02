package es.marcha.backend.services.mail;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import es.marcha.backend.services.media.MediaService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEmailNotificationService {

    private final MailService mailService;
    private final MediaService mService;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Envía el email de solicitud de restablecimiento de contraseña de forma asíncrona.
     *
     * @param name       nombre del usuario
     * @param email      dirección de correo del usuario
     * @param resetToken token UUID para construir el enlace de reset
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String name, String email, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("userName", name);
            ctx.setVariable("resetLink", resetLink);
            ctx.setVariable("hasLogo", logo.isPresent());

            String html = templateEngine.process("emails/user/password-reset", ctx);
            mailService.sendHtmlEmailWithInline(email, "Restablece tu contraseña", html, logo);
            log.info("Email de reset de contraseña enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de reset de contraseña a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía la notificación de cambio de contraseña de forma asíncrona.
     *
     * @param name  nombre del usuario
     * @param email dirección de correo del usuario
     */
    @Async("emailTaskExecutor")
    public void sendPasswordChangeNotification(String name, String email) {
        try {
            String resetLink = frontendUrl + "/reset-password";
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("userName", name);
            ctx.setVariable("resetLink", resetLink);
            ctx.setVariable("hasLogo", logo.isPresent());

            String html = templateEngine.process("emails/user/password-change-notification", ctx);
            mailService.sendHtmlEmailWithInline(email, "Tu contraseña ha sido cambiada", html, logo);
            log.info("Email de confirmación de cambio de contraseña enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar notificación de cambio de contraseña a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de notificación de eliminación de cuenta de forma asíncrona.
     * Debe recibir los datos reales del usuario antes de que sean anonimizados.
     *
     * @param realName     nombre real del usuario (antes de anonimizar)
     * @param realEmail    email real del usuario (antes de anonimizar)
     * @param deletionDate fecha y hora de la eliminación formateada
     * @param userId       ID del usuario (para el log)
     */
    @Async("emailTaskExecutor")
    public void sendAccountDeletionEmail(String realName, String realEmail, String deletionDate, long userId) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("userName", realName);
            ctx.setVariable("userEmail", realEmail);
            ctx.setVariable("deletionDate", deletionDate);
            ctx.setVariable("supportLink", frontendUrl + "/contact");
            ctx.setVariable("hasLogo", logo.isPresent());

            String html = templateEngine.process("emails/user/account-deletion-notification", ctx);
            mailService.sendHtmlEmailWithInline(realEmail, "Notificación de eliminación de cuenta", html, logo);
            log.info("Email de eliminación de cuenta enviado a {}", realEmail);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de eliminación al usuario id {}: {}", userId, e.getMessage());
        }
    }
}
