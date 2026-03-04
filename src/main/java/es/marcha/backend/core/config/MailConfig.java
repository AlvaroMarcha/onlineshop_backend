package es.marcha.backend.core.config;

import java.util.Properties;

import org.springframework.context.annotation.Configuration;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

@Configuration
public class MailConfig {

    /**
     * Creates a JavaMail Session that authenticates via OAuth2 XOAUTH2 mechanism.
     * Jakarta Mail 2.x supports XOAUTH2 natively: supply the access token as the
     * password and set {@code mail.smtp.auth.mechanisms=XOAUTH2}.
     *
     * @param username    the Gmail address used as the sender
     * @param accessToken a valid OAuth2 Bearer access token (short-lived)
     * @return configured {@link Session}
     */
    public static Session getSessionWithOAuth2(String username, String accessToken) {
        Properties props = new Properties();

        // Gmail SMTP over SSL (port 465) — más fiable que STARTTLS/587 con XOAUTH2
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, accessToken);
            }
        });
    }
}
