package es.marcha.backend.modules.dashboard.application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para clientes nuevos por período (rol CUSTOMERS).
 * <p>
 * Representa un cliente recién registrado.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCustomerDTO {

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
     * Indica si el usuario está verificado.
     */
    private boolean isVerified;

    /**
     * Indica si ha realizado algún pedido.
     */
    private boolean hasOrders;

    /**
     * Número de pedidos realizados.
     */
    private long orderCount;
}
