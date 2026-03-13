package es.marcha.backend.modules.dashboard.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import es.marcha.backend.modules.dashboard.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dashboard Controller - Endpoints de métricas y estadísticas para
 * administradores.
 * <p>
 * Acceso restringido a roles SUPER_ADMIN y ADMIN.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ─── Métricas Globales (SUPER_ADMIN) ──────────────────────────────────────

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RevenueResponseDTO> getRevenue(
            @RequestParam(defaultValue = "month") String period) {

        log.info("Obteniendo ingresos para periodo: {}", period);
        RevenueResponseDTO revenue = dashboardService.getRevenue(period);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/chart")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RevenueChartResponseDTO> getRevenueChart(
            @RequestParam(defaultValue = "month") String period) {

        log.info("Obteniendo datos de gráfica de ingresos para periodo: {}", period);
        RevenueChartResponseDTO chartData = dashboardService.getRevenueChart(period);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/conversion-rate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ConversionRateResponseDTO> getConversionRate() {
        log.info("Obteniendo tasa de conversión");
        ConversionRateResponseDTO conversionRate = dashboardService.getConversionRate();
        return ResponseEntity.ok(conversionRate);
    }

    @GetMapping("/average-order-value")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AverageOrderValueResponseDTO> getAverageOrderValue() {
        log.info("Obteniendo valor medio del pedido");
        AverageOrderValueResponseDTO aov = dashboardService.getAverageOrderValue();
        return ResponseEntity.ok(aov);
    }

    // ─── Métricas Operativas (SUPER_ADMIN + ADMIN) ────────────────────────────

    @GetMapping("/orders/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<OrderStatsResponseDTO> getOrderStats() {
        log.info("Obteniendo estadísticas de pedidos");
        OrderStatsResponseDTO stats = dashboardService.getOrderStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserStatsResponseDTO> getUserStats(
            @RequestParam(defaultValue = "week") String period) {

        log.info("Obteniendo estadísticas de usuarios para periodo: {}", period);
        UserStatsResponseDTO stats = dashboardService.getUserStats(period);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/products/top-selling")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Obteniendo top {} productos más vendidos", limit);
        List<TopSellingProductDTO> products = dashboardService.getTopSellingProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<LowStockProductDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {

        log.info("Obteniendo productos con stock <= {}", threshold);
        List<LowStockProductDTO> products = dashboardService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/orders/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<PendingOrderDTO>> getPendingOrders() {
        log.info("Obteniendo pedidos pendientes de gestión");
        List<PendingOrderDTO> pendingOrders = dashboardService.getPendingOrders();
        return ResponseEntity.ok(pendingOrders);
    }

    @GetMapping("/invoices/recent")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<RecentInvoiceDTO>> getRecentInvoices(
            @RequestParam(defaultValue = "20") int limit) {

        log.info("Obteniendo {} facturas recientes", limit);
        List<RecentInvoiceDTO> invoices = dashboardService.getRecentInvoices(limit);
        return ResponseEntity.ok(invoices);
    }
}
