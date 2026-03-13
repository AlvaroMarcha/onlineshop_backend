package es.marcha.backend.modules.dashboard.presentation.controller;

import es.marcha.backend.modules.dashboard.application.dto.response.*;
import es.marcha.backend.modules.dashboard.application.service.StoreDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para estadísticas de catálogo - Rol STORE
 * 
 * Proporciona endpoints especializados para el dashboard del rol STORE:
 * - Resumen de productos (activos, inactivos, sin stock)
 * - Productos más vistos
 * - Productos mejor valorados
 * - Reseñas recientes
 */
@RestController
@RequestMapping("/dashboard/store")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STORE')")
public class StoreDashboardController {

    private final StoreDashboardService storeDashboardService;

    /**
     * Obtiene el resumen del catálogo de productos
     * 
     * Incluye contadores de productos activos, inactivos, sin stock y total.
     * Útil para monitoreo rápido del estado del inventario.
     * 
     * @return ProductSummaryDTO con las métricas del catálogo
     */
    @GetMapping("/product-summary")
    public ResponseEntity<ProductSummaryDTO> getProductSummary() {
        return ResponseEntity.ok(storeDashboardService.getProductSummary());
    }

    /**
     * Obtiene los productos más vistos
     * 
     * Ordenados por número de visualizaciones DESC.
     * Ayuda a identificar productos con mayor interés.
     * 
     * @param limit número máximo de productos a retornar (default: 10)
     * @return Lista de MostViewedProductDTO con datos relevantes
     */
    @GetMapping("/most-viewed")
    public ResponseEntity<List<MostViewedProductDTO>> getMostViewedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(storeDashboardService.getMostViewedProducts(limit));
    }

    /**
     * Obtiene los productos mejor valorados
     * 
     * Ordenados por rating promedio DESC. Solo incluye productos con al menos 1
     * reseña.
     * Útil para destacar productos de calidad o crear secciones promocionales.
     * 
     * @param limit número máximo de productos a retornar (default: 10)
     * @return Lista de BestRatedProductDTO con valoraciones
     */
    @GetMapping("/best-rated")
    public ResponseEntity<List<BestRatedProductDTO>> getBestRatedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(storeDashboardService.getBestRatedProducts(limit));
    }

    /**
     * Obtiene las reseñas más recientes
     * 
     * Ordenadas por fecha de creación DESC.
     * Permite monitorear feedback de clientes en tiempo real.
     * 
     * @param limit número máximo de reseñas a retornar (default: 10)
     * @return Lista de RecentReviewDTO con información de la reseña
     */
    @GetMapping("/recent-reviews")
    public ResponseEntity<List<RecentReviewDTO>> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(storeDashboardService.getRecentReviews(limit));
    }
}
