package es.marcha.backend.core.user.application.mapper;

import es.marcha.backend.core.user.application.dto.response.AdminUserResponseDTO;
import es.marcha.backend.core.user.application.dto.response.UserResponseDTO;
import es.marcha.backend.core.user.domain.model.User;

public class UserMapper {

    public static UserResponseDTO toUserDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleName(user.getRole().getName())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .addresses(user.getAddresses())
                .build();
    }

    /**
     * Mapea una entidad {@link User} al DTO de administración, que incluye
     * todos los campos necesarios para el panel de control:
     * {@code isBanned}, {@code isDeleted}, {@code roleId}, {@code lastLogin}, etc.
     *
     * @param user La entidad a mapear.
     * @return {@link AdminUserResponseDTO} con todos los datos del usuario.
     */
    public static AdminUserResponseDTO toAdminUserDTO(User user) {
        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleName(user.getRole().getName())
                .roleId(user.getRole().getId())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .isBanned(user.isBanned())
                .isDeleted(user.isDeleted())
                .sessionCount(user.getSessionCount())
                .build();
    }

}
