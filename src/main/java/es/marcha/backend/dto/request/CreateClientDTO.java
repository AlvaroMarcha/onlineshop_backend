package es.marcha.backend.dto.request;

import es.marcha.backend.model.Role;

public class CreateClientDTO {
	//Attribs
	private RequestClientDTO client;
	private RequestUserDTO user;
	private Role role;
	
	//Getters and Setters
	
	public RequestClientDTO getClient() {
		return client;
	}
	public void setClient(RequestClientDTO client) {
		this.client = client;
	}
	public RequestUserDTO getUser() {
		return user;
	}
	public void setUser(RequestUserDTO user) {
		this.user = user;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	
	
	
}

