package es.marcha.backend.core.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de petición para cambiar el rol de un usuario.
 * El campo {@code roleName} debe coincidir exactamente con el nombre del rol
 * almacenado en la tabla {@code roles} (p. ej. {@code ROLE_ADMIN}).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangeRoleRequestDTO {

    @NotBlank(message = "El nombre del rol no puede estar vacío")
    private String roleName;
}
