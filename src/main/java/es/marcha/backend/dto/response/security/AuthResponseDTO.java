package es.marcha.backend.dto.response.security;

import es.marcha.backend.dto.response.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class AuthResponseDTO {
    // Attribs
    private UserResponseDTO user;
    private String token;
}
