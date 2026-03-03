package es.marcha.backend.dto.response.user;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para la vista de administración de usuarios.
 * Incluye todos los campos relevantes para el panel de control, entre ellos
 * {@code isBanned}, {@code isDeleted} y la información completa del rol.
 * No se expone la contraseña ni los tokens internos.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class AdminUserResponseDTO {

    private long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String phone;
    private String roleName;
    private long roleId;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private boolean isVerified;
    private boolean isBanned;
    private boolean isDeleted;
    private long sessionCount;
}
