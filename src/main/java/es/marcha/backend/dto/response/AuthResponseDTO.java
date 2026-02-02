package es.marcha.backend.dto.response;

import es.marcha.backend.model.user.User;
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
    private User user;
    private String token;
}
