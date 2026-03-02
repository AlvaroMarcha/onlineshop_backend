package es.marcha.backend.services.user;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
public class UserDeletionService {

    private final UserRepository uRepository;
    private final MailService mailService;
    private final MediaService mService;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Anonimiza y desactiva la cuenta del usuario autenticado conforme al Art. 17 del RGPD.
     * <p>
     * Envía un email de notificación con los datos reales del usuario ANTES de anonimizar.
     * Los datos de carácter personal (nombre, apellido, email, teléfono, imagen) son
     * reemplazados por valores neutros. El username y el email se construyen con el ID
     * para mantener la unicidad en base de datos.
     * <p>
     * Los pedidos vinculados se conservan de forma anónima por obligación legal de 10 años.
     *
     * @param username el username del usuario autenticado extraído del JWT
     * @throws UserException si el usuario no existe en el sistema
     */
    @Transactional
    public void anonymizeAndDelete(String username) {
        User user = uRepository.findByUsername(username)
                .orElseThrow(() -> new UserException());

        String realName = user.getName();
        String realEmail = user.getEmail();
        String deletionDate = LocalDateTime.now().format(FORMATTER);

        sendDeletionEmail(realName, realEmail, deletionDate, user);

        user.setName("Usuario");
        user.setSurname("Eliminado");
        user.setUsername("deleted_" + user.getId());
        user.setEmail("deleted_" + user.getId() + "@eliminado.local");
        user.setPhone("0000000000");
        user.setProfileImageUrl(null);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setTermsAcceptedAt(null);
        user.setTermsVersion(null);
        user.setActive(false);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        uRepository.save(user);
        log.info("Usuario anonimizado y desactivado — id: {}", user.getId());
    }

    /**
     * Envía el email de notificación de eliminación con los datos reales del usuario.
     * Si el envío falla, se registra el error pero no interrumpe el flujo de anonimización.
     *
     * @param realName     nombre real del usuario (antes de anonimizar)
     * @param realEmail    email real del usuario (antes de anonimizar)
     * @param deletionDate fecha y hora de la eliminación formateada
     * @param user         entidad del usuario
     */
    private void sendDeletionEmail(String realName, String realEmail, String deletionDate, User user) {
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
            log.info("Email de eliminación enviado a {}", realEmail);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de eliminación al usuario id {}: {}", user.getId(), e.getMessage());
        }
    }
}
