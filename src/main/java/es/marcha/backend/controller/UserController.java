package es.marcha.backend.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import es.marcha.backend.model.users.User;
import es.marcha.backend.services.UserService;

@RestController
@RequestMapping("api/")
public class UserController {
	//Attribs
	@Autowired
	private  UserService userService;
	
	//ENDPOINTS
	
	//Get
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers(){
		List<User> allUsers = this.userService.getAllUsers();
		return new ResponseEntity<List<User>>(allUsers, HttpStatus.OK);
		
	}
	
	//GetById
	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id){
		Optional<User> userOpt = this.userService.getUserById(id);
		User user = null;
		ResponseEntity<User> response = new ResponseEntity<User>( HttpStatus.NOT_FOUND);
		if(userOpt.isPresent()) {
			user = userOpt.get();
			response = new ResponseEntity<User>(user, HttpStatus.OK);
		}
		return response;
		
	}
	

}
