package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para las métricas de ingresos del dashboard.
 * <p>
 * Contiene el total de ingresos en un periodo específico (hoy, semana, mes,
 * año).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueResponseDTO {

    /**
     * Periodo consultado (today, week, month, year).
     */
    private String period;

    /**
     * Ingresos totales en el periodo.
     */
    private BigDecimal totalRevenue;

    /**
     * Número total de órdenes pagadas en el periodo.
     */
    private long totalOrders;

    /**
     * Fecha de inicio del periodo consultado (formato ISO 8601).
     */
    private String startDate;

    /**
     * Fecha de fin del periodo consultado (formato ISO 8601).
     */
    private String endDate;
}
