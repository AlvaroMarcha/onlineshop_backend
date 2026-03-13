package es.marcha.backend.modules.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para las estadísticas de usuarios del sistema.
 * <p>
 * Contiene información sobre usuarios totales, nuevos y verificados.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponseDTO {

    /**
     * Total de usuarios registrados (excluyendo eliminados y baneados).
     */
    private long totalUsers;

    /**
     * Usuarios registrados en el periodo especificado (hoy, semana, mes).
     */
    private long newUsers;

    /**
     * Periodo utilizado para contar nuevos usuarios (today, week, month).
     */
    private String period;

    /**
     * Total de usuarios verificados (email confirmado).
     */
    private long verifiedUsers;

    /**
     * Total de usuarios no verificados.
     */
    private long unverifiedUsers;

    /**
     * Total de usuarios activos (isActive = true).
     */
    private long activeUsers;

    /**
     * Total de usuarios baneados.
     */
    private long bannedUsers;
}
