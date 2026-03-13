package es.marcha.backend.modules.dashboard.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.modules.dashboard.application.dto.response.OrderWithIssueDTO;
import es.marcha.backend.modules.order.domain.enums.PaymentStatus;

import es.marcha.backend.modules.order.infrastructure.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;

/**
 * Servicio especializado para métricas y operaciones del dashboard de soporte.
 * <p>
 * Proporciona información sobre pedidos con problemas, incidencias y casos que
 * requieren atención.
 * Todos los métodos están cacheados y son de solo lectura.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportDashboardService {

    private final OrderRepository orderRepository;

    /**
     * Obtiene la lista de pedidos con incidencias o problemas de pago.
     * <p>
     * Retorna pedidos que tienen al menos un pago en estado FAILED o REFUNDED,
     * indicando que requieren atención del equipo de soporte.
     * </p>
     *
     * @param limit Número máximo de pedidos a retornar
     * @return Lista de OrderWithIssueDTO ordenada por fecha de creación (más
     *         recientes primero)
     */
    @Cacheable(value = "ordersWithIssues")
    public List<OrderWithIssueDTO> getOrdersWithIssues(int limit) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getPayments() != null && !order.getPayments().isEmpty())
                .filter(order -> {
                    // Verificar si tiene pagos con problemas
                    boolean hasProblems = order.getPayments().stream()
                            .anyMatch(payment -> payment.getStatus() == PaymentStatus.FAILED ||
                                    payment.getStatus() == PaymentStatus.REFUNDED);
                    return hasProblems;
                })
                .sorted((o1, o2) -> {
                    if (o2.getCreatedAt() == null)
                        return -1;
                    if (o1.getCreatedAt() == null)
                        return 1;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                })
                .limit(limit)
                .map(order -> {
                    // Contar items del pedido
                    int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;

                    // Verificar tipos de problemas
                    boolean hasFailedPayments = order.getPayments().stream()
                            .anyMatch(payment -> payment.getStatus() == PaymentStatus.FAILED);

                    boolean hasRefundedPayments = order.getPayments().stream()
                            .anyMatch(payment -> payment.getStatus() == PaymentStatus.REFUNDED);

                    return OrderWithIssueDTO.builder()
                            .orderId(order.getId())
                            .status(order.getStatus().name())
                            .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                            .createdAt(order.getCreatedAt())
                            .customerName(order.getUser().getName() + " " + order.getUser().getSurname())
                            .customerEmail(order.getUser().getEmail())
                            .customerPhone(order.getUser().getPhone())
                            .itemCount(itemCount)
                            .hasFailedPayments(hasFailedPayments)
                            .hasRefundedPayments(hasRefundedPayments)
                            .userId(order.getUser().getId())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
