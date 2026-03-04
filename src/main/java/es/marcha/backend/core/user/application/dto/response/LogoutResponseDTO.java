package es.marcha.backend.core.user.application.dto.response;

import java.time.LocalDateTime;

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
public class LogoutResponseDTO {
    private long userId;
    private String username;
    private boolean isActive;
    private LocalDateTime lastLogin;
}
