package es.marcha.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.marcha.backend.model.users.User;
import es.marcha.backend.repository.UserRepository;

@Service
public class UserService {
	//Attribs
	@Autowired
	private UserRepository userRepository;
	
	
	//CRUD
	
	//GetAllUsers
	public List<User> getAllUsers(){
		return this.userRepository.findAll();
	}
	
	//GetUserById
	public Optional<User> getUserById(Long id){
		return this.userRepository.findById(id);
	}

	//GetUserByUsername
	public Optional<User> getUserByUsername(String username){
		return this.userRepository.findByUsername(username);
	}
	
	//SaveUser
	public User saveUser(User user) {
		return this.userRepository.save(user);
	}
	
	//UpdateUser
	public User updateUser(Long id, User newUser) {
		//Recovery user by id 
		Optional<User> oldUser = this.getUserById(id);
		User user = null;
		if(oldUser.isPresent()) {
			user = oldUser.get();
			BeanUtils.copyProperties(newUser, user, "id", "created_at");
		}
		return this.userRepository.save(user);
	}
	
	//DeleteUser
	public void deleteUser(Long id) {
		this.userRepository.deleteById(id);
	}
	
	

}
