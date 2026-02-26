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
     * Sends a plain-text e-mail via Gmail SMTP using OAuth2 (XOAUTH2).
     * A fresh access token is requested on every call so the token is never stale.
     *
     * @param to      recipient address
     * @param subject e-mail subject
     * @param body    plain-text body
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
