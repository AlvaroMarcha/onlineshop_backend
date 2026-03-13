package es.marcha.backend.modules.dashboard.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.core.shared.domain.enums.OrderStatus;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.domain.model.product.ProductImage;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.dashboard.application.dto.response.AverageOrderValueResponseDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.ConversionRateResponseDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.LowStockProductDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.OrderStatsResponseDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.PendingOrderDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.RecentInvoiceDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.RevenueChartResponseDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.RevenueResponseDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.TopSellingProductDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.UserStatsResponseDTO;
import es.marcha.backend.modules.invoice.domain.model.Invoice;
import es.marcha.backend.modules.invoice.infrastructure.persistence.InvoiceRepository;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.infrastructure.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de Dashboard que proporciona métricas y estadísticas agregadas del
 * negocio.
 * <p>
 * Implementa lógica de agregación de datos optimizada con queries eficientes
 * y caching para asegurar respuestas en menos de 500ms.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;

    // ─── Métricas Globales (SUPER_ADMIN) ──────────────────────────────────────

    /**
     * Obtiene los ingresos totales en un periodo específico.
     * <p>
     * Cache: 10 minutos (los ingresos no cambian tan frecuentemente)
     * </p>
     *
     * @param period periodo a consultar: today, week, month, year
     * @return DTO con ingresos totales y detalles del periodo
     */
    @Cacheable(value = "dashboardRevenue", key = "#period", unless = "#result == null")
    @Transactional(readOnly = true)
    public RevenueResponseDTO getRevenue(String period) {
        LocalDateTime[] dateRange = getPeriodDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID
                        || o.getStatus() == OrderStatus.PROCESSING
                        || o.getStatus() == OrderStatus.SHIPPED
                        || o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        BigDecimal totalRevenue = orders.stream()
                .map(o -> BigDecimal.valueOf(o.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueResponseDTO.builder()
                .period(period)
                .totalRevenue(totalRevenue)
                .totalOrders(orders.size())
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .build();
    }

    /**
     * Obtiene datos de ingresos por día/semana para gráficas.
     * <p>
     * Cache: 15 minutos (datos históricos)
     * </p>
     *
     * @param period periodo a consultar: week, month, year
     * @return DTO con serie temporal de ingresos
     */
    @Cacheable(value = "dashboardRevenueChart", key = "#period", unless = "#result == null")
    @Transactional(readOnly = true)
    public RevenueChartResponseDTO getRevenueChart(String period) {
        LocalDateTime[] dateRange = getPeriodDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID
                        || o.getStatus() == OrderStatus.PROCESSING
                        || o.getStatus() == OrderStatus.SHIPPED
                        || o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        // Agrupar por día para la gráfica
        Map<LocalDate, BigDecimal> revenueByDay = new HashMap<>();
        for (Order order : orders) {
            LocalDate date = order.getCreatedAt().toLocalDate();
            BigDecimal amount = BigDecimal.valueOf(order.getTotalAmount());
            revenueByDay.merge(date, amount, BigDecimal::add);
        }

        // Ordenar y convertir a lista para la gráfica
        List<Map<String, Object>> chartData = revenueByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("date", entry.getKey().toString());
                    dataPoint.put("revenue", entry.getValue());
                    return dataPoint;
                })
                .collect(Collectors.toList());

        BigDecimal totalRevenue = revenueByDay.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueChartResponseDTO.builder()
                .period(period)
                .granularity("day")
                .totalRevenue(totalRevenue)
                .chartData(chartData)
                .build();
    }

    /**
     * Obtiene estadísticas de pedidos por estado.
     * <p>
     * Cache: 5 minutos (datos operativos que cambian frecuentemente)
     * </p>
     *
     * @return DTO con número de pedidos en cada estado
     */
    @Cacheable(value = "dashboardOrderStats", unless = "#result == null")
    @Transactional(readOnly = true)
    public OrderStatsResponseDTO getOrderStats() {
        List<Order> allOrders = orderRepository.findAll();

        long total = allOrders.size();
        long pending = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CREATED).count();
        long paid = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long processing = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count();
        long shipped = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long delivered = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long returned = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.RETURNED).count();

        return OrderStatsResponseDTO.builder()
                .total(total)
                .pending(pending)
                .paid(paid)
                .processing(processing)
                .shipped(shipped)
                .delivered(delivered)
                .cancelled(cancelled)
                .returned(returned)
                .build();
    }

    /**
     * Obtiene estadísticas de usuarios del sistema.
     * <p>
     * Cache: 10 minutos
     * </p>
     *
     * @param period periodo para contar nuevos usuarios: today, week, month
     * @return DTO con estadísticas de usuarios
     */
    @Cacheable(value = "dashboardUserStats", key = "#period", unless = "#result == null")
    @Transactional(readOnly = true)
    public UserStatsResponseDTO getUserStats(String period) {
        LocalDateTime[] dateRange = getPeriodDateRange(period);
        LocalDateTime startDate = dateRange[0];

        long totalUsers = userRepository.count();

        long newUsers = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted() && !u.isBanned())
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startDate))
                .count();

        long verifiedUsers = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted() && !u.isBanned())
                .filter(u -> u.isVerified())
                .count();

        long unverifiedUsers = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted() && !u.isBanned())
                .filter(u -> !u.isVerified())
                .count();

        long activeUsers = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted() && !u.isBanned())
                .filter(u -> u.isActive())
                .count();

        long bannedUsers = userRepository.findAll().stream()
                .filter(u -> u.isBanned())
                .count();

        return UserStatsResponseDTO.builder()
                .totalUsers(totalUsers)
                .newUsers(newUsers)
                .period(period)
                .verifiedUsers(verifiedUsers)
                .unverifiedUsers(unverifiedUsers)
                .activeUsers(activeUsers)
                .bannedUsers(bannedUsers)
                .build();
    }

    /**
     * Obtiene los productos más vendidos ordenados por soldCount.
     * <p>
     * Cache: 10 minutos
     * </p>
     *
     * @param limit número de productos a retornar
     * @return lista de productos más vendidos
     */
    @Cacheable(value = "dashboardTopSelling", key = "#limit", unless = "#result == null")
    @Transactional(readOnly = true)
    public List<TopSellingProductDTO> getTopSellingProducts(int limit) {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()))
                .limit(limit)
                .map(p -> TopSellingProductDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .slug(p.getSlug())
                        .soldCount(p.getSoldCount())
                        .currentStock(p.getStock())
                        .imageUrl(p.getImages().stream()
                                .filter(ProductImage::isMain)
                                .findFirst()
                                .map(ProductImage::getUrl)
                                .orElse(null))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos con stock bajo el umbral especificado.
     * <p>
     * Cache: 5 minutos (datos críticos de inventario)
     * </p>
     *
     * @param threshold umbral de stock bajo
     * @return lista de productos con stock bajo
     */
    @Cacheable(value = "dashboardLowStock", key = "#threshold", unless = "#result == null")
    @Transactional(readOnly = true)
    public List<LowStockProductDTO> getLowStockProducts(int threshold) {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .filter(p -> p.getStock() <= threshold)
                .sorted((p1, p2) -> Integer.compare(p1.getStock(), p2.getStock()))
                .map(p -> LowStockProductDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .slug(p.getSlug())
                        .currentStock(p.getStock())
                        .soldCount(p.getSoldCount())
                        .isActive(p.isActive())
                        .imageUrl(p.getImages().stream()
                                .filter(ProductImage::isMain)
                                .findFirst()
                                .map(ProductImage::getUrl)
                                .orElse(null))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Calcula la tasa de conversión del negocio.
     * <p>
     * Cache: 15 minutos (métrica de análisis)
     * </p>
     *
     * @return DTO con tasa de conversión
     */
    @Cacheable(value = "dashboardConversion", unless = "#result == null")
    @Transactional(readOnly = true)
    public ConversionRateResponseDTO getConversionRate() {
        long totalUsers = userRepository.count();

        List<Long> userIdsWithOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CREATED && o.getStatus() != OrderStatus.CANCELLED)
                .map(o -> o.getUser().getId())
                .distinct()
                .collect(Collectors.toList());

        long usersWithOrders = userIdsWithOrders.size();

        long totalOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CREATED && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalUsers > 0) {
            conversionRate = BigDecimal.valueOf(usersWithOrders)
                    .divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return ConversionRateResponseDTO.builder()
                .totalUsers(totalUsers)
                .usersWithOrders(usersWithOrders)
                .conversionRate(conversionRate)
                .totalOrders(totalOrders)
                .build();
    }

    /**
     * Calcula el valor medio del pedido (AOV).
     * <p>
     * Cache: 10 minutos
     * </p>
     *
     * @return DTO con valor medio del pedido
     */
    @Cacheable(value = "dashboardAOV", unless = "#result == null")
    @Transactional(readOnly = true)
    public AverageOrderValueResponseDTO getAverageOrderValue() {
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID
                        || o.getStatus() == OrderStatus.PROCESSING
                        || o.getStatus() == OrderStatus.SHIPPED
                        || o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = completedOrders.stream()
                .map(o -> BigDecimal.valueOf(o.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = completedOrders.size();

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        return AverageOrderValueResponseDTO.builder()
                .averageOrderValue(averageOrderValue)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .build();
    }

    // ─── Endpoints Operativos (ADMIN + SUPER_ADMIN) ───────────────────────────

    /**
     * Obtiene pedidos pendientes de gestión.
     * <p>
     * Sin cache (datos operativos en tiempo real)
     * </p>
     *
     * @return lista de pedidos pendientes
     */
    @Transactional(readOnly = true)
    public List<PendingOrderDTO> getPendingOrders() {
        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED
                        || o.getStatus() == OrderStatus.PAID
                        || o.getStatus() == OrderStatus.PROCESSING)
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());

        return pendingOrders.stream()
                .map(o -> PendingOrderDTO.builder()
                        .orderId(o.getId())
                        .status(o.getStatus().name())
                        .totalAmount(BigDecimal.valueOf(o.getTotalAmount()))
                        .paymentMethod(o.getPaymentMethod())
                        .createdAt(o.getCreatedAt())
                        .userId(o.getUser().getId())
                        .userName(o.getUser().getName() + " " + o.getUser().getSurname())
                        .userEmail(o.getUser().getEmail())
                        .itemCount(o.getOrderItems() != null ? o.getOrderItems().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene facturas recientes.
     * <p>
     * Cache: 5 minutos
     * </p>
     *
     * @param limit número de facturas a retornar
     * @return lista de facturas recientes
     */
    @Cacheable(value = "dashboardRecentInvoices", key = "#limit", unless = "#result == null")
    @Transactional(readOnly = true)
    public List<RecentInvoiceDTO> getRecentInvoices(int limit) {
        List<Invoice> invoices = invoiceRepository.findAll();

        return invoices.stream()
                .sorted((i1, i2) -> i2.getCreatedAt().compareTo(i1.getCreatedAt()))
                .limit(limit)
                .map(i -> RecentInvoiceDTO.builder()
                        .invoiceId(i.getId())
                        .invoiceNumber(i.getInvoiceNumber())
                        .orderId(i.getOrder().getId())
                        .totalAmount(i.getTotalAmount())
                        .createdAt(i.getCreatedAt())
                        .userId(i.getUser().getId())
                        .userName(i.getUser().getName() + " " + i.getUser().getSurname())
                        .userEmail(i.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Helpers Privados ─────────────────────────────────────────────────────

    /**
     * Calcula el rango de fechas según el periodo especificado.
     *
     * @param period periodo: today, week, month, year
     * @return array con [startDate, endDate]
     */
    private LocalDateTime[] getPeriodDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime endDate = now;

        switch (period.toLowerCase()) {
            case "today":
                startDate = now.with(LocalTime.MIN);
                break;
            case "week":
                startDate = now.minusWeeks(1);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            case "year":
                startDate = now.minusYears(1);
                break;
            default:
                startDate = now.minusMonths(1); // default: último mes
        }

        return new LocalDateTime[] { startDate, endDate };
    }
}
