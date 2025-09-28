package es.marcha.backend.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResponseUserDTO {
    private String name;
    private String username;
    private String email;
    private String phone;
    private boolean status;
    private LocalDateTime email_verified_at;
    private boolean locked;
    private LocalDateTime lastLogin_at;
    private LocalDateTime created_at;
    private Long roleId; 

}
