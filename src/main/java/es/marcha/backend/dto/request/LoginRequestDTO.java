package es.marcha.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDTO {
    private String user;
    private String pass;

    LoginRequestDTO(String user, String pass){
        this.user = user;
        this.pass = pass;   
    }
}
