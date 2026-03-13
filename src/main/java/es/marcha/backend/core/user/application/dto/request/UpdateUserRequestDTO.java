package es.marcha.backend.core.user.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de petición para actualizar los datos del usuario autenticado.
 * Todos los campos son opcionales: solo se actualiza lo que se envía.
 * La contraseña no puede cambiarse desde este endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateUserRequestDTO {

    @Email(message = "El email no tiene un formato válido")
    private String email;

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String surname;

    private String phone;
}
