package es.marcha.backend.mapper;

import es.marcha.backend.dto.response.user.UserResponseDTO;
import es.marcha.backend.model.user.User;

public class UserMapper {

    public static UserResponseDTO toUserDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId()).name(user.getName())
                .surname(user.getSurname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleName(user.getRole().getName())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                // .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .addresses(user.getAddresses())
                .build();
    }

}
