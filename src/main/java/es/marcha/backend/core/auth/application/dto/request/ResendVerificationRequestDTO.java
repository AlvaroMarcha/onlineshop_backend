package es.marcha.backend.core.auth.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResendVerificationRequestDTO {

    /**
     * Email del usuario que solicita reenvío del email de verificación.
     */
    private String email;
}
