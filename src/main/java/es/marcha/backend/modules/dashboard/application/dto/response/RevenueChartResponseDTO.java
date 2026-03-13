package es.marcha.backend.modules.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la gráfica de ingresos por día/semana.
 * <p>
 * Contiene una serie temporal de ingresos para visualización en gráfica.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueChartResponseDTO {

    /**
     * Periodo consultado (week, month, year).
     */
    private String period;

    /**
     * Granularidad de los datos (day, week).
     */
    private String granularity;

    /**
     * Total de ingresos en el periodo completo.
     */
    private BigDecimal totalRevenue;

    /**
     * Datos para la gráfica: lista de mapas con keys "date" y "revenue".
     * Ejemplo: [{"date": "2026-03-13", "revenue": 1250.50}, ...]
     */
    private List<Map<String, Object>> chartData;
}
