package es.marcha.backend.modules.dashboard.application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para clientes baneados (rol CUSTOMERS).
 * <p>
 * Representa un usuario que ha sido baneado del sistema.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannedCustomerDTO {

    /**
     * ID del usuario.
     */
    private long userId;

    /**
     * Nombre completo.
     */
    private String name;

    /**
     * Apellido.
     */
    private String surname;

    /**
     * Email.
     */
    private String email;

    /**
     * Fecha de registro.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha del último inicio de sesión.
     */
    private LocalDateTime lastLogin;

    /**
     * Número de pedidos realizados antes del baneo.
     */
    private long orderCount;
}
