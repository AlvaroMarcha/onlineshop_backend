package es.marcha.backend.services.order;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import es.marcha.backend.services.mail.MailService;
import es.marcha.backend.services.media.MediaService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderConfirmationEmailService {

    private final MailService mailService;
    private final MediaService mService;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envía el email de confirmación de pedido de forma asíncrona.
     * <p>
     * Si el envío falla por cualquier motivo (SMTP, Thymeleaf, etc.), el error se
     * registra pero no se propaga: el pedido ya está creado en BD y la respuesta al
     * cliente no debe verse afectada.
     *
     * @param user     el usuario que realizó el pedido
     * @param order    la orden persistida con su ID y total
     * @param items    los ítems snapshot de la orden
     * @param address  el snapshot de la dirección de envío
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
            ctx.setVariable("orderDate", order.getCreatedAt().format(DATE_FMT));
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
}
