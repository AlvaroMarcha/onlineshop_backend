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
public class LoginRequestDTO {
    // Attribs
    private String usernameOrEmail;
    private String password;

}
