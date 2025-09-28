package es.marcha.backend.dto.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RequestUserDTO {
	private String name;
    private String username;
    private String password;
    private String email;
    private String phone;
    private boolean status;
    private LocalDateTime emailVerifiedAt;
    private boolean locked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Long roleId; 

}
