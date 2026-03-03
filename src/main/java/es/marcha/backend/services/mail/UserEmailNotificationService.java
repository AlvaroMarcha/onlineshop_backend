package es.marcha.backend.services.mail;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import es.marcha.backend.model.enums.OrderStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import es.marcha.backend.dto.response.order.OrderAddrResponseDTO;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.OrderItems;
import es.marcha.backend.model.user.User;
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
            ctx.setVariable("orderLink", frontendUrl + "/orders/" + order.getId());
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
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
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
            ctx.setVariable("orderLink", frontendUrl + "/orders/" + order.getId());
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
            ctx.setVariable("orderLink", frontendUrl + "/orders/" + order.getId());
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
