package es.marcha.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAuthDTO {
    //Attribs
    private String token;
    private ResponseUserDTO user;
	private String message;

    public ResponseAuthDTO(String token, ResponseUserDTO user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
    }

}
