package es.marcha.backend.dto.request.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PasswordResetConfirmDTO {
    private String token;
    private String newPassword;
}
