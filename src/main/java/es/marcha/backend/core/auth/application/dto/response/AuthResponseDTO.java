package es.marcha.backend.core.auth.application.dto.response;

import es.marcha.backend.core.user.application.dto.response.UserResponseDTO;
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
    /** Access token JWT (validez 60 minutos). */
    private String token;
    /** Refresh token UUID (validez 30 días). Usar en POST /auth/refresh. */
    private String refreshToken;
}
