package es.marcha.backend.modules.dashboard.presentation.controller;

import es.marcha.backend.modules.dashboard.application.dto.response.OrderWithIssueDTO;
import es.marcha.backend.modules.dashboard.application.service.SupportDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para estadísticas de soporte - Rol SUPPORT
 * 
 * Proporciona endpoints especializados para el dashboard del rol SUPPORT:
 * - Pedidos con problemas (pagos fallidos o reembolsados)
 * - Tickets abiertos (placeholder para futura implementación)
 */
@RestController
@RequestMapping("/dashboard/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPPORT')")
public class SupportDashboardController {

    private final SupportDashboardService supportDashboardService;

    /**
     * Obtiene pedidos con problemas de pago
     * 
     * Filtra pedidos que tienen pagos en estado FAILED o REFUNDED.
     * Permite al equipo de soporte identificar casos que requieren atención.
     * 
     * @param limit número máximo de pedidos a retornar (default: 20)
     * @return Lista de OrderWithIssueDTO con información del problema
     */
    @GetMapping("/orders-with-issues")
    public ResponseEntity<List<OrderWithIssueDTO>> getOrdersWithIssues(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(supportDashboardService.getOrdersWithIssues(limit));
    }
}
