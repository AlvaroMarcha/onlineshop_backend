package es.marcha.backend.modules.dashboard.presentation.controller;

import es.marcha.backend.modules.dashboard.application.dto.response.*;
import es.marcha.backend.modules.dashboard.application.service.CustomersDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para estadísticas de clientes - Rol CUSTOMERS_INVOICES
 * 
 * Proporciona endpoints especializados para el dashboard del rol CUSTOMERS:
 * - Nuevos clientes por período
 * - Mejores compradores
 * - Clientes baneados
 * - Métricas de retención
 */
@RestController
@RequestMapping("/dashboard/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMERS_INVOICES')")
public class CustomersDashboardController {

    private final CustomersDashboardService customersDashboardService;

    /**
     * Obtiene los nuevos clientes registrados en un período
     * 
     * Períodos soportados: week, month, quarter, year.
     * Incluye información de verificación y actividad inicial.
     * 
     * @param period período temporal de análisis (default: week)
     * @param limit  número máximo de clientes a retornar (default: 20)
     * @return Lista de NewCustomerDTO con datos de registro
     */
    @GetMapping("/new-customers")
    public ResponseEntity<List<NewCustomerDTO>> getNewCustomers(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(customersDashboardService.getNewCustomers(period, limit));
    }

    /**
     * Obtiene los clientes con mayor gasto total
     * 
     * Ordenados por suma de totalAmount de sus pedidos.
     * Útil para programas de fidelización o atención VIP.
     * 
     * @param limit número máximo de clientes a retornar (default: 10)
     * @return Lista de TopBuyerDTO con métricas de compra
     */
    @GetMapping("/top-buyers")
    public ResponseEntity<List<TopBuyerDTO>> getTopBuyers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(customersDashboardService.getTopBuyers(limit));
    }

    /**
     * Obtiene la lista de clientes baneados
     * 
     * Muestra usuarios con isBanned=true para seguimiento.
     * Incluye información de actividad para revisión.
     * 
     * @return Lista de BannedCustomerDTO con detalles del usuario
     */
    @GetMapping("/banned-customers")
    public ResponseEntity<List<BannedCustomerDTO>> getBannedCustomers() {
        return ResponseEntity.ok(customersDashboardService.getBannedCustomers());
    }

    /**
     * Obtiene métricas de retención de clientes
     * 
     * Calcula:
     * - Total de clientes registrados
     * - Clientes con al menos 1 pedido (conversión)
     * - Clientes con 2+ pedidos (recurrentes)
     * - Tasas de conversión y retención
     * 
     * @return CustomerRetentionDTO con KPIs de retención
     */
    @GetMapping("/retention")
    public ResponseEntity<CustomerRetentionDTO> getCustomerRetention() {
        return ResponseEntity.ok(customersDashboardService.getCustomerRetention());
    }
}
