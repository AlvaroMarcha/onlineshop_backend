package es.marcha.backend.controller.mail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.services.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/mails")
public class MailController {

    private final MailService mailService;

    /**
     * Envía un email de prueba mediante Gmail SMTP con autenticación OAuth2.
     * <p>
     * Útil para verificar que la integración con Google OAuth2 funciona correctamente.
     * El destinatario, asunto y cuerpo del mensaje están definidos de forma fija para este endpoint de pruebas.
     * </p>
     *
     * @return {@link ResponseEntity} con mensaje de confirmación y código HTTP 200 OK,
     *         o mensaje de error y código HTTP 500 si el envío falla.
     */
    @PostMapping("/testing/send")
    public ResponseEntity<String> sendTestEmail() {
        try {
            mailService.sendEmail(
                    "alanmarcha.2000@gmail.com",
                    "Testing from Marcha_backend",
                    "Hello, this is a test email from Marcha_backend via OAuth2!");

            return ResponseEntity.ok("Email enviado correctamente");
        } catch (Exception e) {
            log.error("Error sending test email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el email: " + e.getMessage());
        }
    }

}
