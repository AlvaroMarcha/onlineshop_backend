package es.marcha.backend.modules.notification.application.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import es.marcha.backend.core.shared.domain.enums.OrderStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import es.marcha.backend.modules.order.application.dto.response.OrderAddrResponseDTO;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.domain.model.OrderItems;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.filestorage.application.service.MediaService;
import es.marcha.backend.core.notification.infrastructure.mail.MailService;
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

    private static final DateTimeFormatter ORDER_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envía el email de solicitud de restablecimiento de contraseña de forma
     * asíncrona.
     *
     * @param name       nombre del usuario
     * @param email      dirección de correo del usuario
     * @param resetToken token UUID para construir el enlace de reset
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String name, String email, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password.html?token=" + resetToken;
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
            String resetLink = frontendUrl + "/reset-password.html";
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

    /**
     * Envía el email de confirmación de pedido de forma asíncrona.
     * Si el envío falla, el error se registra pero no se propaga: el pedido ya
     * está creado en BD y la respuesta al cliente no se ve afectada.
     *
     * @param user    el usuario que realizó el pedido
     * @param order   la orden persistida con su ID y total
     * @param items   los ítems snapshot de la orden
     * @param address el snapshot de la dirección de envío
     */
    @Async("emailTaskExecutor")
    public void sendOrderConfirmationEmail(User user, Order order,
            List<OrderItems> items, OrderAddrResponseDTO address) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            List<Map<String, Object>> itemsData = items.stream().map(item -> {
                BigDecimal effectivePrice = (item.getDiscountPrice() != null
                        && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                                ? item.getDiscountPrice()
                                : item.getPrice();

                Map<String, Object> row = new HashMap<>();
                row.put("name", item.getName());
                row.put("quantity", item.getQuantity());
                row.put("price", String.format("%.2f €", effectivePrice));
                return row;
            }).collect(Collectors.toList());

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", user.getName() + " " + user.getSurname());
            ctx.setVariable("orderId", order.getId());
            ctx.setVariable("orderDate", order.getCreatedAt().format(ORDER_DATE_FMT));
            ctx.setVariable("orderItems", itemsData);
            ctx.setVariable("orderTotal", String.format("%.2f €", order.getTotalAmount()));
            ctx.setVariable("orderLink", frontendUrl + "/pages/orders.html");
            ctx.setVariable("shippingAddress", address);

            String html = templateEngine.process("emails/orders/order-confirmation", ctx);
            mailService.sendHtmlEmailWithInline(
                    user.getEmail(),
                    "Confirmación de pedido #" + order.getId(),
                    html,
                    logo);

            log.info("Email de confirmación enviado a {} para el pedido #{}", user.getEmail(), order.getId());
        } catch (IOException | MessagingException e) {
            log.error("Error enviando email de confirmación del pedido #{}: {}", order.getId(), e.getMessage());
        }
    }

    /**
     * Envía el email de verificación de cuenta de forma asíncrona.
     * <p>
     * Incluye un enlace con el token de verificación que expira en 24 horas.
     * El fallo en el envío se registra pero no interrumpe el registro del usuario.
     * </p>
     *
     * @param name              nombre del usuario
     * @param email             dirección de correo del usuario
     * @param verificationToken token UUID para construir el enlace de verificación
     */
    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String name, String email, String verificationToken) {
        try {
            String verificationLink = frontendUrl + "/verify-email.html?token=" + verificationToken;
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("userName", name);
            ctx.setVariable("verificationLink", verificationLink);
            ctx.setVariable("hasLogo", logo.isPresent());

            String html = templateEngine.process("emails/user/user-verification", ctx);
            mailService.sendHtmlEmailWithInline(email, "Verifica tu cuenta", html, logo);
            log.info("Email de verificación enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de verificación a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de actualización de estado de pedido de forma asíncrona.
     * Incluye un tracker visual con los pasos del pedido y un banner según el
     * estado.
     * El fallo en el envío se registra pero no interrumpe la transición de estado.
     *
     * @param user  el usuario propietario del pedido
     * @param order la orden con el nuevo estado ya persistido
     * @param items los ítems snapshot de la orden
     */
    @Async("emailTaskExecutor")
    public void sendOrderStatusUpdateEmail(User user, Order order, List<OrderItems> items) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();
            OrderStatus status = order.getStatus();

            // Construir datos de los artículos del pedido
            List<Map<String, Object>> itemsData = items.stream().map(item -> {
                BigDecimal effectivePrice = (item.getDiscountPrice() != null
                        && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                                ? item.getDiscountPrice()
                                : item.getPrice();
                Map<String, Object> row = new HashMap<>();
                row.put("name", item.getName());
                row.put("quantity", item.getQuantity());
                row.put("price", String.format("%.2f €", effectivePrice));
                return row;
            }).collect(Collectors.toList());

            // Construir pasos del tracker y datos del banner
            List<Map<String, Object>> steps = buildOrderSteps(status);
            String[] banner = buildBannerData(status);

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", user.getName() + " " + user.getSurname());
            ctx.setVariable("orderId", order.getId());
            ctx.setVariable("orderDate", order.getCreatedAt().format(ORDER_DATE_FMT));
            ctx.setVariable("steps", steps);
            ctx.setVariable("bannerClass", banner[0]);
            ctx.setVariable("statusTitle", banner[1]);
            ctx.setVariable("statusMessage", banner[2]);
            ctx.setVariable("orderItems", itemsData);
            ctx.setVariable("orderTotal", String.format("%.2f €", order.getTotalAmount()));
            ctx.setVariable("orderLink", frontendUrl + "/pages/orders.html");
            ctx.setVariable("supportLink", frontendUrl + "/contact");

            String html = templateEngine.process("emails/orders/order-status-update", ctx);
            mailService.sendHtmlEmailWithInline(
                    user.getEmail(),
                    "Actualización de tu pedido #" + order.getId(),
                    html,
                    logo);

            log.info("Email de actualización de estado enviado a {} para el pedido #{}",
                    user.getEmail(), order.getId());
        } catch (IOException | MessagingException e) {
            log.error("Error enviando email de actualización del pedido #{}: {}",
                    order.getId(), e.getMessage());
        }
    }

    /**
     * Envía el email de actualización de estado de pedido cuando este pasa a PAID,
     * adjuntando la factura PDF generada automáticamente.
     * Si el envío falla, el error se registra pero no interrumpe el flujo.
     *
     * @param user            el usuario propietario del pedido
     * @param order           la orden con el estado PAID ya persistido
     * @param items           los ítems snapshot de la orden
     * @param invoicePdfPath  ruta en disco del PDF de la factura
     * @param invoiceFileName nombre del archivo que verá el destinatario (p. ej.
     *                        INV-2026-000001.pdf)
     */
    @Async("emailTaskExecutor")
    public void sendOrderStatusUpdateEmailWithInvoice(User user, Order order, List<OrderItems> items,
            String invoicePdfPath, String invoiceFileName) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();
            OrderStatus status = order.getStatus();

            // Construir datos de los artículos del pedido
            List<Map<String, Object>> itemsData = items.stream().map(item -> {
                BigDecimal effectivePrice = (item.getDiscountPrice() != null
                        && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                                ? item.getDiscountPrice()
                                : item.getPrice();
                Map<String, Object> row = new HashMap<>();
                row.put("name", item.getName());
                row.put("quantity", item.getQuantity());
                row.put("price", String.format("%.2f €", effectivePrice));
                return row;
            }).collect(Collectors.toList());

            // Construir pasos del tracker y datos del banner
            List<Map<String, Object>> steps = buildOrderSteps(status);
            String[] banner = buildBannerData(status);

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", user.getName() + " " + user.getSurname());
            ctx.setVariable("orderId", order.getId());
            ctx.setVariable("orderDate", order.getCreatedAt().format(ORDER_DATE_FMT));
            ctx.setVariable("steps", steps);
            ctx.setVariable("bannerClass", banner[0]);
            ctx.setVariable("statusTitle", banner[1]);
            ctx.setVariable("statusMessage", banner[2]);
            ctx.setVariable("orderItems", itemsData);
            ctx.setVariable("orderTotal", String.format("%.2f €", order.getTotalAmount()));
            ctx.setVariable("orderLink", frontendUrl + "/pages/orders.html");
            ctx.setVariable("supportLink", frontendUrl + "/contact");

            String html = templateEngine.process("emails/orders/order-status-update", ctx);
            mailService.sendHtmlEmailWithInlineAndAttachment(
                    user.getEmail(),
                    "Pago confirmado – Factura de tu pedido #" + order.getId(),
                    html,
                    logo,
                    invoicePdfPath,
                    invoiceFileName);

            log.info("Email de pago confirmado con factura enviado a {} para el pedido #{}",
                    user.getEmail(), order.getId());
        } catch (IOException | MessagingException e) {
            log.error("Error enviando email con factura del pedido #{}: {}",
                    order.getId(), e.getMessage());
        }
    }

    /**
     * Envía el email de bienvenida tras la verificación de la cuenta de forma
     * asíncrona.
     * <p>
     * Este email se envía automáticamente cuando el usuario verifica su correo
     * electrónico, dándole la bienvenida al sistema.
     * </p>
     *
     * @param name  nombre del usuario
     * @param email dirección de correo del usuario
     */
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String name, String email) {
        try {
            String dashboardLink = frontendUrl + "/pages/user.html";
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("userName", name);
            ctx.setVariable("dashboardLink", dashboardLink);
            ctx.setVariable("hasLogo", logo.isPresent());

            String html = templateEngine.process("emails/user/welcome-email", ctx);
            mailService.sendHtmlEmailWithInline(email, "¡Bienvenido!", html, logo);
            log.info("Email de bienvenida enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de bienvenida a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de notificación de cuenta bloqueada de forma asíncrona.
     * <p>
     * Este método está disponible para notificar al usuario cuando su cuenta
     * ha sido bloqueada por actividades sospechosas o intentos fallidos de acceso.
     * Actualmente debe llamarse manualmente desde el admin.
     * </p>
     *
     * @param name  nombre del usuario
     * @param email dirección de correo del usuario
     */
    @Async("emailTaskExecutor")
    public void sendAccountLockedEmail(String name, String email) {
        try {
            String recoveryLink = frontendUrl + "/reset-password.html";
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("userName", name);
            ctx.setVariable("recoveryLink", recoveryLink);
            ctx.setVariable("hasLogo", logo.isPresent());

            String html = templateEngine.process("emails/user/account-locked", ctx);
            mailService.sendHtmlEmailWithInline(email, "Cuenta bloqueada", html, logo);
            log.info("Email de cuenta bloqueada enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de cuenta bloqueada a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email con la factura en PDF adjunta bajo demanda.
     * <p>
     * Este método se utiliza cuando un cliente o admin solicita reenviar la factura
     * de un pedido ya completado. A diferencia del envío automático al pasar a
     * PAID,
     * este es un reenvío manual.
     * </p>
     *
     * @param user            el usuario propietario del pedido
     * @param order           la orden asociada a la factura
     * @param items           los ítems snapshot de la orden
     * @param invoicePdfPath  ruta en disco del PDF de la factura
     * @param invoiceFileName nombre del archivo (ej. INV-2026-000001.pdf)
     */
    @Async("emailTaskExecutor")
    public void sendInvoiceEmail(User user, Order order, List<OrderItems> items,
            String invoicePdfPath, String invoiceFileName) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            List<Map<String, Object>> itemsData = items.stream().map(item -> {
                BigDecimal effectivePrice = (item.getDiscountPrice() != null
                        && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                                ? item.getDiscountPrice()
                                : item.getPrice();
                Map<String, Object> row = new HashMap<>();
                row.put("name", item.getName());
                row.put("quantity", item.getQuantity());
                row.put("price", String.format("%.2f €", effectivePrice));
                return row;
            }).collect(Collectors.toList());

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", user.getName() + " " + user.getSurname());
            ctx.setVariable("orderNumber", "#" + order.getId());
            ctx.setVariable("orderItems", itemsData);
            ctx.setVariable("orderTotal", String.format("%.2f €", order.getTotalAmount()));
            ctx.setVariable("orderLink", frontendUrl + "/pages/orders.html");

            String html = templateEngine.process("emails/orders/invoice-email", ctx);
            mailService.sendHtmlEmailWithInlineAndAttachment(
                    user.getEmail(),
                    "Factura de tu pedido #" + order.getId(),
                    html,
                    logo,
                    invoicePdfPath,
                    invoiceFileName);

            log.info("Email de factura enviado bajo demanda a {} para el pedido #{}",
                    user.getEmail(), order.getId());
        } catch (IOException | MessagingException e) {
            log.error("Error enviando email de factura del pedido #{}: {}",
                    order.getId(), e.getMessage());
        }
    }

    /**
     * Envía el email de notificación de devolución/reembolso procesado.
     * <p>
     * Este método se llama automáticamente cuando un Payment pasa al estado
     * REFUNDED,
     * informando al usuario sobre los artículos devueltos y el monto reembolsado.
     * </p>
     *
     * @param user          el usuario propietario del pedido
     * @param order         la orden asociada
     * @param returnedItems lista de ítems devueltos con sus montos reembolsados
     * @param totalRefund   monto total reembolsado
     */
    @Async("emailTaskExecutor")
    public void sendReturnRefundNotification(User user, Order order,
            List<Map<String, Object>> returnedItems, BigDecimal totalRefund) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", user.getName() + " " + user.getSurname());
            ctx.setVariable("orderNumber", "#" + order.getId());
            ctx.setVariable("returnedItems", returnedItems);
            ctx.setVariable("totalRefund", String.format("%.2f €", totalRefund));
            ctx.setVariable("orderLink", frontendUrl + "/pages/orders.html");

            String html = templateEngine.process("emails/orders/return-refund-notification", ctx);
            mailService.sendHtmlEmailWithInline(
                    user.getEmail(),
                    "Devolución procesada - Pedido #" + order.getId(),
                    html,
                    logo);

            log.info("Email de devolución/reembolso enviado a {} para el pedido #{}",
                    user.getEmail(), order.getId());
        } catch (IOException | MessagingException e) {
            log.error("Error enviando email de devolución del pedido #{}: {}",
                    order.getId(), e.getMessage());
        }
    }

    /**
     * Envía el email con un cupón de descuento personalizado.
     * <p>
     * Este método debe llamarse manualmente desde el panel de administración
     * cuando se crea y asigna un cupón a un cliente específico.
     * </p>
     *
     * @param userName      nombre del usuario destinatario
     * @param email         dirección de correo del usuario
     * @param couponCode    código del cupón (ej: DESCUENTO20)
     * @param expiryDate    fecha de expiración formateada (ej: 31/12/2026)
     * @param useCouponLink enlace directo para usar el cupón
     */
    @Async("emailTaskExecutor")
    public void sendDiscountCouponEmail(String userName, String email, String couponCode,
            String expiryDate, String useCouponLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("couponCode", couponCode);
            ctx.setVariable("expiryDate", expiryDate);
            ctx.setVariable("useCouponLink", useCouponLink);

            String html = templateEngine.process("emails/orders/discount-cuppon", ctx);
            mailService.sendHtmlEmailWithInline(email, "¡Cupón exclusivo para ti!", html, logo);
            log.info("Email de cupón de descuento enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de cupón a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email con una oferta personalizada basada en los intereses del
     * cliente.
     * <p>
     * Este método debe llamarse manualmente desde el panel de administración o
     * sistema de marketing cuando se lanza una campaña personalizada.
     * </p>
     *
     * @param userName         nombre del usuario destinatario
     * @param email            dirección de correo del usuario
     * @param offerBanner      URL de la imagen/banner de la oferta
     * @param offerDescription descripción de la oferta
     * @param offerLink        enlace directo a la oferta
     */
    @Async("emailTaskExecutor")
    public void sendPersonalizedOfferEmail(String userName, String email, String offerBanner,
            String offerDescription, String offerLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("offerBanner", offerBanner);
            ctx.setVariable("offerDescription", offerDescription);
            ctx.setVariable("offerLink", offerLink);

            String html = templateEngine.process("emails/orders/personalized-offer", ctx);
            mailService.sendHtmlEmailWithInline(email, "Oferta especial para ti", html, logo);
            log.info("Email de oferta personalizada enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de oferta personalizada a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de newsletter con contenido destacado y ofertas.
     * <p>
     * Este método debe llamarse manualmente desde el panel de administración o
     * sistema de marketing. Soporta un bloque principal destacado y múltiples
     * bloques secundarios.
     * </p>
     *
     * @param email           dirección de correo del destinatario
     * @param newsletterTitle título de la newsletter
     * @param introText       texto introductorio
     * @param mainItem        mapa con datos del bloque principal (badge, title,
     *                        imageUrl, summary, link) - puede ser null
     * @param secondaryItems  lista de mapas con datos de bloques secundarios
     *                        (badge,
     *                        title, imageUrl, summary, link)
     */
    @Async("emailTaskExecutor")
    public void sendNewsletterEmail(String email, String newsletterTitle, String introText,
            Map<String, String> mainItem, List<Map<String, String>> secondaryItems) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("newsletterTitle", newsletterTitle);
            ctx.setVariable("introText", introText);
            ctx.setVariable("mainItem", mainItem);
            ctx.setVariable("secondaryItems", secondaryItems != null ? secondaryItems : new ArrayList<>());

            String html = templateEngine.process("emails/newsletter", ctx);
            mailService.sendHtmlEmailWithInline(email, newsletterTitle, html, logo);
            log.info("Newsletter enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar newsletter a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de encuesta de satisfacción post-compra.
     * <p>
     * Este método debe llamarse manualmente desde el panel de administración.
     * En el futuro puede automatizarse para enviarse X días después de la entrega.
     * </p>
     *
     * @param userName   nombre del usuario destinatario
     * @param email      dirección de correo del usuario
     * @param introText  texto introductorio personalizado (opcional, puede ser
     *                   null)
     * @param surveyLink enlace a la encuesta externa
     * @param showStars  si mostrar o no el rating visual de estrellas
     */
    @Async("emailTaskExecutor")
    public void sendSatisfactionSurveyEmail(String userName, String email, String introText,
            String surveyLink, boolean showStars) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            String defaultIntro = "Tu opinión es muy importante para nosotros. "
                    + "Por favor, responde nuestra breve encuesta de satisfacción.";

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("introText", introText != null ? introText : defaultIntro);
            ctx.setVariable("showStars", showStars);
            ctx.setVariable("surveyLink", surveyLink);

            String html = templateEngine.process("emails/satisfaction-survey", ctx);
            mailService.sendHtmlEmailWithInline(email, "Queremos tu opinión", html, logo);
            log.info("Email de encuesta de satisfacción enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de encuesta a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de notificación de cuenta baneada de forma asíncrona.
     * <p>
     * Este método debe llamarse cuando un administrador banea permanentemente una
     * cuenta de usuario, informándole sobre el baneo y las razones del mismo.
     * </p>
     *
     * @param userName    nombre del usuario baneado
     * @param email       dirección de correo del usuario
     * @param banReason   razón del baneo
     * @param banDate     fecha formateada del baneo
     * @param supportLink enlace al soporte para apelaciones
     */
    @Async("emailTaskExecutor")
    public void sendAccountBannedEmail(String userName, String email, String banReason,
            String banDate, String supportLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            String defaultReason = "Tu cuenta ha sido baneada debido al incumplimiento de nuestras políticas "
                    + "de uso y términos de servicio.";

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("banReason", banReason != null ? banReason : defaultReason);
            ctx.setVariable("banDate", banDate);
            ctx.setVariable("supportLink", supportLink != null ? supportLink : frontendUrl + "/contact");

            String html = templateEngine.process("emails/user/account-banned", ctx);
            mailService.sendHtmlEmailWithInline(email, "Notificación de baneo de cuenta", html, logo);
            log.info("Email de notificación de baneo enviado a {}", email);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de baneo a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de confirmación de creación de ticket al usuario.
     * <p>
     * Este método se llama cuando un usuario crea un nuevo ticket de soporte,
     * confirmándole la recepción y proporcionándole los detalles del ticket.
     * </p>
     *
     * @param userName              nombre del usuario
     * @param email                 dirección de correo del usuario
     * @param ticketNumber          número del ticket (ej: #12345)
     * @param ticketSubject         asunto del ticket
     * @param ticketCategory        categoría del ticket (ej: Pedidos, Técnico)
     * @param ticketPriority        prioridad del ticket (Baja, Media, Alta)
     * @param createdDate           fecha de creación formateada
     * @param userMessage           mensaje inicial del usuario
     * @param estimatedResponseTime tiempo estimado de respuesta (ej: 24-48 horas)
     * @param ticketLink            enlace para ver el ticket
     */
    @Async("emailTaskExecutor")
    public void sendTicketCreatedToUser(String userName, String email, String ticketNumber,
            String ticketSubject, String ticketCategory, String ticketPriority,
            String createdDate, String userMessage, String estimatedResponseTime,
            String ticketLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("ticketNumber", ticketNumber);
            ctx.setVariable("ticketSubject", ticketSubject);
            ctx.setVariable("ticketCategory", ticketCategory);
            ctx.setVariable("ticketPriority", ticketPriority);
            ctx.setVariable("createdDate", createdDate);
            ctx.setVariable("userMessage", userMessage);
            ctx.setVariable("estimatedResponseTime",
                    estimatedResponseTime != null ? estimatedResponseTime : "24-48 horas");
            ctx.setVariable("ticketLink", ticketLink);

            String html = templateEngine.process("emails/support/ticket-user", ctx);
            mailService.sendHtmlEmailWithInline(email,
                    "Ticket de soporte creado - " + ticketNumber, html, logo);
            log.info("Email de creación de ticket enviado al usuario {} - Ticket: {}", email, ticketNumber);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de creación de ticket a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de notificación de nuevo ticket al equipo de administración.
     * <p>
     * Este método se llama cuando se crea un nuevo ticket de soporte, notificando
     * al equipo de admin/soporte para que pueda atenderlo.
     * </p>
     *
     * @param adminEmail      dirección de correo del admin/soporte
     * @param ticketNumber    número del ticket (ej: #12345)
     * @param ticketSubject   asunto del ticket
     * @param ticketCategory  categoría del ticket
     * @param ticketPriority  prioridad del ticket (Baja, Media, Alta)
     * @param createdDate     fecha de creación formateada
     * @param ticketStatus    estado actual del ticket (ej: Abierto)
     * @param userName        nombre del cliente que creó el ticket
     * @param userEmail       email del cliente
     * @param orderId         ID del pedido relacionado (puede ser null)
     * @param userMessage     mensaje del cliente
     * @param adminTicketLink enlace al panel de admin para gestionar el ticket
     */
    @Async("emailTaskExecutor")
    public void sendTicketCreatedToAdmin(String adminEmail, String ticketNumber,
            String ticketSubject, String ticketCategory, String ticketPriority,
            String createdDate, String ticketStatus, String userName, String userEmail,
            String orderId, String userMessage, String adminTicketLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("ticketNumber", ticketNumber);
            ctx.setVariable("ticketSubject", ticketSubject);
            ctx.setVariable("ticketCategory", ticketCategory);
            ctx.setVariable("ticketPriority", ticketPriority);
            ctx.setVariable("createdDate", createdDate);
            ctx.setVariable("ticketStatus", ticketStatus != null ? ticketStatus : "Abierto");
            ctx.setVariable("userName", userName);
            ctx.setVariable("userEmail", userEmail);
            ctx.setVariable("orderId", orderId);
            ctx.setVariable("userMessage", userMessage);
            ctx.setVariable("adminTicketLink", adminTicketLink);

            String html = templateEngine.process("emails/support/ticket-admin", ctx);
            mailService.sendHtmlEmailWithInline(adminEmail,
                    "[NUEVO TICKET] " + ticketNumber + " - " + ticketSubject, html, logo);
            log.info("Email de nuevo ticket enviado al admin {} - Ticket: {}", adminEmail, ticketNumber);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de nuevo ticket al admin {}: {}", adminEmail, e.getMessage());
        }
    }

    /**
     * Envía el email de actualización de ticket al usuario.
     * <p>
     * Este método se llama cuando un miembro del equipo de soporte responde o
     * actualiza un ticket existente, notificando al usuario sobre el progreso.
     * </p>
     *
     * @param userName        nombre del usuario
     * @param email           dirección de correo del usuario
     * @param ticketNumber    número del ticket
     * @param ticketSubject   asunto del ticket
     * @param ticketStatus    estado actual del ticket (ej: En progreso)
     * @param updateDate      fecha de la actualización formateada
     * @param respondentName  nombre del miembro del equipo que respondió
     * @param responseMessage mensaje de respuesta del equipo
     * @param nextSteps       próximos pasos (puede ser null)
     * @param ticketLink      enlace para ver el ticket
     */
    @Async("emailTaskExecutor")
    public void sendTicketUpdateEmail(String userName, String email, String ticketNumber,
            String ticketSubject, String ticketStatus, String updateDate,
            String respondentName, String responseMessage, String nextSteps,
            String ticketLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("ticketNumber", ticketNumber);
            ctx.setVariable("ticketSubject", ticketSubject);
            ctx.setVariable("ticketStatus", ticketStatus);
            ctx.setVariable("updateDate", updateDate);
            ctx.setVariable("respondentName", respondentName);
            ctx.setVariable("responseMessage", responseMessage);
            ctx.setVariable("nextSteps", nextSteps);
            ctx.setVariable("ticketLink", ticketLink);

            String html = templateEngine.process("emails/support/ticket-update", ctx);
            mailService.sendHtmlEmailWithInline(email,
                    "Actualización de tu ticket " + ticketNumber, html, logo);
            log.info("Email de actualización de ticket enviado a {} - Ticket: {}", email, ticketNumber);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de actualización de ticket a {}: {}", email, e.getMessage());
        }
    }

    /**
     * Envía el email de cierre de ticket al usuario.
     * <p>
     * Este método se llama cuando un ticket de soporte es cerrado, notificando al
     * usuario sobre la resolución y opcionalmente solicitando feedback.
     * </p>
     *
     * @param userName          nombre del usuario
     * @param email             dirección de correo del usuario
     * @param ticketNumber      número del ticket
     * @param ticketSubject     asunto del ticket
     * @param createdDate       fecha de creación del ticket formateada
     * @param closedDate        fecha de cierre del ticket formateada
     * @param resolutionTime    tiempo total de resolución (ej: 3 días)
     * @param closedBy          nombre de quien cerró el ticket
     * @param resolutionNotes   notas de resolución (puede ser null)
     * @param includeSurvey     si incluir enlace a encuesta de satisfacción
     * @param surveyLink        enlace a la encuesta (requerido si includeSurvey es
     *                          true)
     * @param ticketHistoryLink enlace para ver el historial completo del ticket
     */
    @Async("emailTaskExecutor")
    public void sendTicketClosedEmail(String userName, String email, String ticketNumber,
            String ticketSubject, String createdDate, String closedDate,
            String resolutionTime, String closedBy, String resolutionNotes,
            boolean includeSurvey, String surveyLink, String ticketHistoryLink) {
        try {
            Optional<FileSystemResource> logo = mService.getCompanyLogoResource();

            Context ctx = new Context();
            ctx.setVariable("hasLogo", logo.isPresent());
            ctx.setVariable("userName", userName);
            ctx.setVariable("ticketNumber", ticketNumber);
            ctx.setVariable("ticketSubject", ticketSubject);
            ctx.setVariable("createdDate", createdDate);
            ctx.setVariable("closedDate", closedDate);
            ctx.setVariable("resolutionTime", resolutionTime);
            ctx.setVariable("closedBy", closedBy);
            ctx.setVariable("resolutionNotes", resolutionNotes);
            ctx.setVariable("includeSurvey", includeSurvey);
            ctx.setVariable("surveyLink", surveyLink);
            ctx.setVariable("ticketHistoryLink", ticketHistoryLink);

            String html = templateEngine.process("emails/support/ticket-close", ctx);
            mailService.sendHtmlEmailWithInline(email,
                    "Ticket cerrado - " + ticketNumber, html, logo);
            log.info("Email de cierre de ticket enviado a {} - Ticket: {}", email, ticketNumber);
        } catch (IOException | MessagingException e) {
            log.error("Error al enviar email de cierre de ticket a {}: {}", email, e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /**
     * Construye la lista de pasos del tracker según el estado actual del pedido.
     * Los 5 pasos del flujo normal siempre están presentes; si el estado es
     * CANCELLED o RETURNED se añade un paso terminal adicional.
     *
     * @param status el estado actual de la orden
     * @return lista de mapas con las claves "label", "state" e "icon"
     */
    private List<Map<String, Object>> buildOrderSteps(OrderStatus status) {
        // Etiquetas de los 5 pasos del flujo normal
        String[] labels = { "Pedido recibido", "Pago confirmado", "En preparación", "Enviado", "Entregado" };

        // Índice del paso activo en el flujo normal (-1 para estados terminales
        // externos)
        int activeIdx = switch (status) {
            case CREATED -> 0;
            case PAID -> 1;
            case PROCESSING -> 2;
            case SHIPPED -> 3;
            case DELIVERED -> 4;
            default -> -1;
        };

        List<Map<String, Object>> steps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String state;
            if (activeIdx == -1) {
                // CANCELLED / RETURNED: pasos normales en pending
                state = "pending";
            } else if (status == OrderStatus.DELIVERED) {
                // Todos completados al entregar
                state = "completed";
            } else if (i < activeIdx) {
                state = "completed";
            } else if (i == activeIdx) {
                state = "active";
            } else {
                state = "pending";
            }

            Map<String, Object> step = new HashMap<>();
            step.put("label", labels[i]);
            step.put("state", state);
            step.put("icon", stateIcon(state));
            steps.add(step);
        }

        // Paso terminal opcional
        if (status == OrderStatus.CANCELLED) {
            Map<String, Object> step = new HashMap<>();
            step.put("label", "Cancelado");
            step.put("state", "cancelled");
            step.put("icon", "\u2717"); // ✗
            steps.add(step);
        } else if (status == OrderStatus.RETURNED) {
            Map<String, Object> step = new HashMap<>();
            step.put("label", "Devuelto");
            step.put("state", "returned");
            step.put("icon", "\u21a9"); // ↩
            steps.add(step);
        }

        return steps;
    }

    /**
     * Devuelve el icono Unicode correspondiente al estado de un paso del tracker.
     *
     * @param state estado del paso (completed, active, cancelled, returned,
     *              pending)
     * @return string con el carácter Unicode del icono
     */
    private String stateIcon(String state) {
        return switch (state) {
            case "completed" -> "\u2713"; // ✓
            case "active" -> "\u25cf"; // ●
            case "cancelled" -> "\u2717"; // ✗
            case "returned" -> "\u21a9"; // ↩
            default -> "\u25cb"; // ○
        };
    }

    /**
     * Devuelve los datos del banner de estado: [bannerClass, statusTitle,
     * statusMessage].
     *
     * @param status el estado actual de la orden
     * @return array de 3 strings con clase CSS, título y mensaje
     */
    private String[] buildBannerData(OrderStatus status) {
        return switch (status) {
            case CREATED -> new String[] { "banner-orange", "Pedido recibido",
                    "Hemos recibido tu pedido y estamos a la espera del pago." };
            case PAID -> new String[] { "banner-blue", "Pago confirmado",
                    "Tu pago ha sido procesado correctamente." };
            case PROCESSING -> new String[] { "banner-blue", "En preparación",
                    "Estamos preparando tu pedido." };
            case SHIPPED -> new String[] { "banner-blue", "Enviado",
                    "Tu pedido está en camino." };
            case DELIVERED -> new String[] { "banner-green", "Entregado",
                    "Tu pedido ha sido entregado con éxito." };
            case CANCELLED -> new String[] { "banner-red", "Pedido cancelado",
                    "Tu pedido ha sido cancelado." };
            case RETURNED -> new String[] { "banner-orange", "Devolución procesada",
                    "Hemos recibido la devolución de tu pedido." };
        };
    }
}
