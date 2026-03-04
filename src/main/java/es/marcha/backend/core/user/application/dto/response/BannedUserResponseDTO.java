package es.marcha.backend.core.user.application.dto.response;

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
public class BannedUserResponseDTO {
    // Attribs
    private long id;
    private String username;
    private boolean isBanned;
    private boolean isActive;

}
