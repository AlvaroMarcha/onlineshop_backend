package es.marcha.backend.modules.dashboard.application.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.core.shared.domain.enums.OrderStatus;
import es.marcha.backend.modules.dashboard.application.dto.response.DelayedShipmentDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.OrderQueueItemDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.PendingRefundDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.TodayOrdersSummaryDTO;
import es.marcha.backend.modules.order.domain.enums.PaymentStatus;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.infrastructure.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;

/**
 * Servicio especializado para métricas y operaciones del dashboard de pedidos.
 * <p>
 * Proporciona resúmenes, colas de trabajo y alertas relacionadas con pedidos y
 * pagos.
 * Todos los métodos están cacheados y son de solo lectura.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdersDashboardService {

    private final OrderRepository orderRepository;

    /**
     * Obtiene el resumen de pedidos del día actual.
     * <p>
     * Calcula: total de pedidos hoy, ingresos totales, pedidos pendientes y
     * completados.
     * </p>
     *
     * @return TodayOrdersSummaryDTO con las estadísticas del día
     */
    @Cacheable(value = "todayOrdersSummary")
    public TodayOrdersSummaryDTO getTodayOrdersSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Order> todayOrders = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt() != null &&
                        order.getCreatedAt().isAfter(startOfDay) &&
                        order.getCreatedAt().isBefore(endOfDay))
                .collect(Collectors.toList());

        long totalOrders = todayOrders.size();

        double totalRevenue = todayOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        long pendingOrders = todayOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CREATED ||
                        order.getStatus() == OrderStatus.PROCESSING)
                .count();

        long completedOrders = todayOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();

        return TodayOrdersSummaryDTO.builder()
                .totalOrders(totalOrders)
                .totalRevenue(BigDecimal.valueOf(totalRevenue))
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .build();
    }

    /**
     * Obtiene la cola de pedidos pendientes de procesamiento.
     * <p>
     * Retorna pedidos en estado CREATED o PROCESSING, ordenados por antigüedad (más
     * antiguos primero).
     * </p>
     *
     * @param limit Número máximo de pedidos a retornar
     * @return Lista de OrderQueueItemDTO ordenada por antigüedad
     */
    @Cacheable(value = "orderQueue")
    public List<OrderQueueItemDTO> getOrderQueue(int limit) {
        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.CREATED ||
                        order.getStatus() == OrderStatus.PROCESSING)
                .sorted((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());

        return pendingOrders.stream()
                .map(order -> {
                    long hoursSinceCreation = order.getCreatedAt() != null
                            ? Duration.between(order.getCreatedAt(), LocalDateTime.now()).toHours()
                            : 0;

                    return OrderQueueItemDTO.builder()
                            .orderId(order.getId())
                            .status(order.getStatus().name())
                            .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                            .createdAt(order.getCreatedAt())
                            .customerName(order.getUser().getName() + " " + order.getUser().getSurname())
                            .customerEmail(order.getUser().getEmail())
                            .hoursSinceCreation(hoursSinceCreation)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de pagos en estado SUCCESS que potencialmente necesitan
     * reembolso.
     * <p>
     * Este método retorna pagos exitosos que podrían requerir procesamiento de
     * reembolso
     * en un futuro flujo manual (ej: devoluciones, cancelaciones post-pago).
     * Nota: Para reembolsos reales, verificar que el pedido esté en estado
     * RETURNED.
     * </p>
     *
     * @param limit Número máximo de pagos a retornar
     * @return Lista de PendingRefundDTO
     */
    @Cacheable(value = "pendingRefunds")
    public List<PendingRefundDTO> getPendingRefunds(int limit) {
        // Obtener pedidos en estado RETURNED que tengan pagos SUCCESS
        List<Order> ordersNeedingRefund = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.RETURNED)
                .filter(order -> order.getPayments() != null && !order.getPayments().isEmpty())
                .filter(order -> order.getPayments().stream()
                        .anyMatch(payment -> payment.getStatus() == PaymentStatus.SUCCESS))
                .limit(limit)
                .collect(Collectors.toList());

        return ordersNeedingRefund.stream()
                .flatMap(order -> order.getPayments().stream()
                        .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                        .map(payment -> PendingRefundDTO.builder()
                                .paymentId(payment.getId())
                                .orderId(order.getId())
                                .amount(BigDecimal.valueOf(payment.getAmount()))
                                .paymentMethod(order.getPaymentMethod())
                                .orderCreatedAt(order.getCreatedAt())
                                .customerName(order.getUser().getName() + " " + order.getUser().getSurname())
                                .customerEmail(order.getUser().getEmail())
                                .stripePaymentIntentId(payment.getTransactionId())
                                .build()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de pedidos con envíos retrasados.
     * <p>
     * Un pedido se considera retrasado si está en estado PROCESSING y han pasado
     * más de 7 días
     * desde su creación. La fecha estimada de entrega se calcula como createdAt + 7
     * días.
     * </p>
     *
     * @param limit Número máximo de pedidos a retornar
     * @return Lista de DelayedShipmentDTO ordenada por días de retraso
     *         (descendente)
     */
    @Cacheable(value = "delayedShipments")
    public List<DelayedShipmentDTO> getDelayedShipments(int limit) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Order> delayedOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PROCESSING)
                .filter(order -> order.getCreatedAt() != null &&
                        order.getCreatedAt().isBefore(sevenDaysAgo))
                .collect(Collectors.toList());

        return delayedOrders.stream()
                .map(order -> {
                    LocalDateTime estimatedDeliveryDate = order.getCreatedAt().plusDays(7);
                    long daysDelayed = ChronoUnit.DAYS.between(estimatedDeliveryDate, LocalDateTime.now());

                    // Obtener dirección de envío del primer OrderItem si existe
                    String shippingAddress = "N/A";
                    if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                        // Obtener dirección del usuario
                        if (order.getUser() != null && order.getUser().getAddresses() != null &&
                                !order.getUser().getAddresses().isEmpty()) {
                            var address = order.getUser().getAddresses().get(0);
                            shippingAddress = String.format("%s, %s, %s",
                                    address.getAddressLine1() != null ? address.getAddressLine1() : "",
                                    address.getCity() != null ? address.getCity() : "",
                                    address.getPostalCode() != null ? address.getPostalCode() : "");
                        }
                    }

                    return DelayedShipmentDTO.builder()
                            .orderId(order.getId())
                            .status(order.getStatus().name())
                            .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                            .createdAt(order.getCreatedAt())
                            .estimatedDeliveryDate(estimatedDeliveryDate)
                            .daysDelayed(daysDelayed)
                            .customerName(order.getUser().getName() + " " + order.getUser().getSurname())
                            .customerEmail(order.getUser().getEmail())
                            .shippingAddress(shippingAddress)
                            .build();
                })
                .sorted((d1, d2) -> Long.compare(d2.getDaysDelayed(), d1.getDaysDelayed()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
