package es.marcha.backend.core.user.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import es.marcha.backend.core.error.exception.UserException;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.modules.notification.application.service.UserEmailNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDeletionService {

    private final UserRepository uRepository;
    private final UserEmailNotificationService userEmailNotificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Anonimiza y desactiva la cuenta del usuario autenticado conforme al Art. 17
     * del RGPD.
     * <p>
     * Envía un email de notificación con los datos reales del usuario ANTES de
     * anonimizar.
     * Los datos de carácter personal (nombre, apellido, email, teléfono, imagen)
     * son
     * reemplazados por valores neutros. El username y el email se construyen con el
     * ID
     * para mantener la unicidad en base de datos.
     * <p>
     * Los pedidos vinculados se conservan de forma anónima por obligación legal de
     * 10 años.
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

        userEmailNotificationService.sendAccountDeletionEmail(realName, realEmail, deletionDate, user.getId());

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
}
