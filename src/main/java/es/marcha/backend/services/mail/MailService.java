package es.marcha.backend.services.mail;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import es.marcha.backend.config.MailConfig;
import es.marcha.backend.services.mail.google.GoogleOAuthService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
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
     * Envía un correo electrónico de texto plano mediante Gmail SMTP con
     * autenticación OAuth2 (XOAUTH2).
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

    /**
     * Envía un correo electrónico en formato HTML mediante Gmail SMTP con
     * autenticación OAuth2.
     *
     * @param to       Dirección de correo del destinatario.
     * @param subject  Asunto del mensaje.
     * @param htmlBody Cuerpo del mensaje en HTML.
     * @throws MessagingException si ocurre un error durante el envío por SMTP.
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        String accessToken = googleOAuthService.getAccessToken();
        Session session = MailConfig.getSessionWithOAuth2(username, accessToken);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
        log.info("HTML email sent to {} with subject '{}'", to, subject);
    }

    /**
     * Envía un correo HTML con el logotipo de la empresa incrustado como adjunto
     * inline (CID), de forma que los clientes de correo lo muestren sin necesidad
     * de cargar recursos externos.
     *
     * <p>
     * Si {@code logo} está vacío, el mensaje se envía como HTML plano y la
     * etiqueta {@code <img th:if="">} de las plantillas no se renderiza.
     *
     * @param to       Dirección de correo del destinatario.
     * @param subject  Asunto del mensaje.
     * @param htmlBody Cuerpo del mensaje en HTML (con {@code src="cid:logoImage"}).
     * @param logo     Recurso del archivo de logo a incrustar, o vacío.
     * @throws MessagingException si ocurre un error durante el envío por SMTP.
     * @throws IOException 
     */
    public void sendHtmlEmailWithInline(String to, String subject, String htmlBody,
            Optional<FileSystemResource> logo) throws MessagingException, IOException {
        String accessToken = googleOAuthService.getAccessToken();
        Session session = MailConfig.getSessionWithOAuth2(username, accessToken);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        if (logo.isPresent()) {
            MimeMultipart multipart = new MimeMultipart("related");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.attachFile(logo.get().getFile());
            imagePart.setContentID("<logoImage>");
            imagePart.setDisposition(Part.INLINE);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);
        } else {
            message.setContent(htmlBody, "text/html; charset=UTF-8");
        }

        Transport.send(message);
        log.info("HTML email with inline sent to {} with subject '{}'", to, subject);
    }
}
