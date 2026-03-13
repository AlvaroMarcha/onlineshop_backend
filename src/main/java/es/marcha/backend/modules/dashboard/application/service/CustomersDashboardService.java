package es.marcha.backend.modules.dashboard.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.modules.dashboard.application.dto.response.BannedCustomerDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.CustomerRetentionDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.NewCustomerDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.TopBuyerDTO;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.infrastructure.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;

/**
 * Servicio especializado para métricas y operaciones del dashboard de clientes.
 * <p>
 * Proporciona estadísticas de usuarios, nuevos clientes, top compradores y
 * retención.
 * Todos los métodos están cacheados y son de solo lectura.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomersDashboardService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * Obtiene la lista de nuevos clientes registrados en un período específico.
     * <p>
     * Períodos soportados: "week", "month", "quarter", "year".
     * Por defecto usa "week" si el período no es válido.
     * </p>
     *
     * @param period Período a consultar (week/month/quarter/year)
     * @param limit  Número máximo de clientes a retornar
     * @return Lista de NewCustomerDTO ordenada por fecha de registro (descendente)
     */
    @Cacheable(value = "newCustomers")
    public List<NewCustomerDTO> getNewCustomers(String period, int limit) {
        LocalDateTime since = switch (period.toLowerCase()) {
            case "week" -> LocalDateTime.now().minusWeeks(1);
            case "month" -> LocalDateTime.now().minusMonths(1);
            case "quarter" -> LocalDateTime.now().minusMonths(3);
            case "year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusWeeks(1);
        };

        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted() && !user.isBanned())
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(since))
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(user -> {
                    // Contar pedidos del usuario
                    List<Order> userOrders = orderRepository.findAllByUserId(user.getId());
                    long orderCount = userOrders.size();
                    boolean hasOrders = orderCount > 0;

                    return NewCustomerDTO.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .surname(user.getSurname())
                            .email(user.getEmail())
                            .createdAt(user.getCreatedAt())
                            .isVerified(user.isVerified())
                            .hasOrders(hasOrders)
                            .orderCount(orderCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el ranking de clientes que más han gastado.
     * <p>
     * Ordena clientes por la suma total de sus pedidos (totalAmount) en orden
     * descendente.
     * Solo incluye usuarios activos y no baneados con al menos un pedido.
     * </p>
     *
     * @param limit Número máximo de clientes a retornar
     * @return Lista de TopBuyerDTO ordenada por gasto total (descendente)
     */
    @Cacheable(value = "topBuyers")
    public List<TopBuyerDTO> getTopBuyers(int limit) {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted() && !user.isBanned())
                .map(user -> {
                    List<Order> orders = orderRepository.findAllByUserId(user.getId());

                    double totalSpent = orders.stream()
                            .mapToDouble(Order::getTotalAmount)
                            .sum();

                    long orderCount = orders.size();

                    double averageOrderValue = orderCount > 0 ? totalSpent / orderCount : 0.0;

                    return TopBuyerDTO.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .surname(user.getSurname())
                            .email(user.getEmail())
                            .totalSpent(BigDecimal.valueOf(totalSpent))
                            .orderCount(orderCount)
                            .averageOrderValue(BigDecimal.valueOf(averageOrderValue))
                            .build();
                })
                .filter(buyer -> buyer.getTotalSpent().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(TopBuyerDTO::getTotalSpent, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de clientes baneados del sistema.
     * <p>
     * Retorna todos los usuarios con isBanned = true, ordenados por fecha de
     * registro.
     * </p>
     *
     * @return Lista de BannedCustomerDTO
     */
    @Cacheable(value = "bannedCustomers")
    public List<BannedCustomerDTO> getBannedCustomers() {
        return userRepository.findAll().stream()
                .filter(User::isBanned)
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(user -> {
                    // Contar pedidos del usuario antes del baneo
                    List<Order> userOrders = orderRepository.findAllByUserId(user.getId());
                    long orderCount = userOrders.size();

                    return BannedCustomerDTO.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .surname(user.getSurname())
                            .email(user.getEmail())
                            .createdAt(user.getCreatedAt())
                            .lastLogin(user.getLastLogin())
                            .orderCount(orderCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene métricas de retención de clientes.
     * <p>
     * Calcula:
     * - Total de clientes registrados
     * - Clientes con al menos un pedido
     * - Clientes recurrentes (con más de un pedido)
     * - Tasa de retención (recurrentes / con pedidos)
     * - Tasa de conversión (con pedidos / total)
     * </p>
     *
     * @return CustomerRetentionDTO con las métricas calculadas
     */
    @Cacheable(value = "customerRetention")
    public CustomerRetentionDTO getCustomerRetention() {
        List<User> allUsers = userRepository.findAll().stream()
                .filter(user -> !user.isDeleted() && !user.isBanned())
                .collect(Collectors.toList());

        long totalCustomers = allUsers.size();

        // Obtener todos los pedidos y agrupar por usuario
        Map<Long, Long> orderCounts = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getId(), Collectors.counting()));

        long customersWithOrders = orderCounts.values().stream()
                .filter(count -> count > 0)
                .count();

        long recurringCustomers = orderCounts.values().stream()
                .filter(count -> count > 1)
                .count();

        Double retentionRate = customersWithOrders > 0 ? (double) recurringCustomers / customersWithOrders * 100 : 0.0;

        Double conversionRate = totalCustomers > 0 ? (double) customersWithOrders / totalCustomers * 100 : 0.0;

        return CustomerRetentionDTO.builder()
                .totalCustomers(totalCustomers)
                .customersWithOrders(customersWithOrders)
                .recurringCustomers(recurringCustomers)
                .retentionRate(Math.round(retentionRate * 100.0) / 100.0) // 2 decimales
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .build();
    }
}
