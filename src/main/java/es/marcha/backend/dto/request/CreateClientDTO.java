package es.marcha.backend.dto.request;

import es.marcha.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateClientDTO {
	//Attribs
	private RequestClientDTO client;
	private RequestUserDTO user;
	private Role role;
	
	
	
}

