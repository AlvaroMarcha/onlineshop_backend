package es.marcha.backend.core.auth.application.dto.request;

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
public class LogoutRequestDTO {
    private long userId;
    /** Refresh token a revocar en el logout. Opcional pero recomendado. */
    private String refreshToken;
}
