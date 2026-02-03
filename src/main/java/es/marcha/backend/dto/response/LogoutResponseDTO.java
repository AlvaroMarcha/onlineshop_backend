package es.marcha.backend.dto.response;

import java.util.Date;

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
    private Date lastLogin;
}
