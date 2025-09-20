package es.marcha.backend.dto.response;

import es.marcha.backend.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAuthDTO {
    //Attribs
    private String token;
    private User user;

    public ResponseAuthDTO(String token, User user) {
        this.token = token;
        this.user = user;
    }

}
