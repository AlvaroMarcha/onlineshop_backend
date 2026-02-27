package es.marcha.backend.services.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.marcha.backend.config.MailConfig;
import es.marcha.backend.services.mail.google.GoogleOAuthService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

    @Value("${app.mail.username}")
    private String username;

    private final GoogleOAuthService googleOAuthService;

    /**
     * Envía un correo electrónico de texto plano mediante Gmail SMTP con autenticación OAuth2 (XOAUTH2).
     * <p>
     * Obtiene un access token fresco de {@link GoogleOAuthService} en cada llamada,
     * garantizando que el token nunca esté caducado en el momento del envío.
     * </p>
     *
     * @param to      Dirección de correo del destinatario.
     * @param subject Asunto del mensaje.
     * @param body    Cuerpo del mensaje en texto plano.
     * @throws MessagingException si ocurre un error durante el envío por SMTP.
     */
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        String accessToken = googleOAuthService.getAccessToken();
        Session session = MailConfig.getSessionWithOAuth2(username, accessToken);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        log.info("Email sent to {} with subject '{}'", to, subject);
    }
}
