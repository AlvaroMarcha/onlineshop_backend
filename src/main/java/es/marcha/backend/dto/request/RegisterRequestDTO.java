package es.marcha.backend.dto.request;

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
public class RegisterRequestDTO {
    // Attribs
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private String phone;

}
