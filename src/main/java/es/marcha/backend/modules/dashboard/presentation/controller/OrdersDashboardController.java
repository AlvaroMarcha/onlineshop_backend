package es.marcha.backend.modules.dashboard.presentation.controller;

import es.marcha.backend.modules.dashboard.application.dto.response.*;
import es.marcha.backend.modules.dashboard.application.service.OrdersDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para estadísticas de pedidos - Rol ORDERS
 * 
 * Proporciona endpoints especializados para el dashboard del rol ORDERS:
 * - Resumen de pedidos del día
 * - Cola de pedidos pendientes
 * - Reembolsos pendientes
 * - Envíos retrasados
 */
@RestController
@RequestMapping("/dashboard/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORDERS')")
public class OrdersDashboardController {

    private final OrdersDashboardService ordersDashboardService;

    /**
     * Obtiene el resumen de pedidos del día actual
     * 
     * Incluye: total de pedidos, ingresos totales, pedidos pendientes y
     * completados.
     * Datos cacheados con refresh periódico.
     * 
     * @return TodayOrdersSummaryDTO con las métricas del día
     */
    @GetMapping("/today-summary")
    public ResponseEntity<TodayOrdersSummaryDTO> getTodayOrdersSummary() {
        return ResponseEntity.ok(ordersDashboardService.getTodayOrdersSummary());
    }

    /**
     * Obtiene la cola de pedidos pendientes de procesamiento
     * 
     * Filtra pedidos en estado CREATED o PROCESSING, ordenados por antigüedad.
     * Útil para priorizar el procesamiento de pedidos.
     * 
     * @param limit número máximo de pedidos a retornar (default: 10)
     * @return Lista de OrderQueueItemDTO con información prioritaria
     */
    @GetMapping("/queue")
    public ResponseEntity<List<OrderQueueItemDTO>> getOrderQueue(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ordersDashboardService.getOrderQueue(limit));
    }

    /**
     * Obtiene la lista de reembolsos pendientes
     * 
     * Muestra pagos que requieren procesamiento de reembolso.
     * Incluye información de Stripe para facilitar el proceso.
     * 
     * @param limit número máximo de reembolsos a retornar (default: 10)
     * @return Lista de PendingRefundDTO con detalles del pago
     */
    @GetMapping("/pending-refunds")
    public ResponseEntity<List<PendingRefundDTO>> getPendingRefunds(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ordersDashboardService.getPendingRefunds(limit));
    }

    /**
     * Obtiene los envíos retrasados
     * 
     * Identifica pedidos en procesamiento que superan el tiempo estimado de entrega
     * (7 días).
     * Permite tomar acciones correctivas o notificar al cliente.
     * 
     * @param limit número máximo de envíos a retornar (default: 10)
     * @return Lista de DelayedShipmentDTO con información del retraso
     */
    @GetMapping("/delayed-shipments")
    public ResponseEntity<List<DelayedShipmentDTO>> getDelayedShipments(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ordersDashboardService.getDelayedShipments(limit));
    }
}
