package es.marcha.backend.dto.response.user;

import java.time.LocalDateTime;
import java.util.List;

import es.marcha.backend.model.user.Address;
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
public class UserResponseDTO {
    // Attribs
    private long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String phone;
    private String roleName;
    // private String profileImageUrl;
    private LocalDateTime createdAt;
    private boolean isActive;
    private boolean isVerified;
    private List<Address> addresses;
}
