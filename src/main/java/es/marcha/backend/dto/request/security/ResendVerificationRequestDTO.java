package es.marcha.backend.dto.request.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResendVerificationRequestDTO {

    /**
     * Username o email del usuario que solicita reenvío del email de verificación.
     */
    private String usernameOrEmail;
}
