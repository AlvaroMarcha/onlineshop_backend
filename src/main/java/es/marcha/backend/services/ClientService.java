package es.marcha.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.model.Client;
import es.marcha.backend.model.Role;
import es.marcha.backend.model.User;
import es.marcha.backend.repository.ClientRepository;

@Service
public class ClientService {
	
	@Autowired
    private ClientRepository clientRepository;
	
	
	//GetAllClients
	public List<Client> getAllClients(){
		return this.clientRepository.findAll();
		
	}
	
	//GetClientById
	public Optional<Client> getClientById(Long id){
		return this.clientRepository.findById(id);
	}

	//CreateClientWithUser
	public Client createClientWithUser(Client clientRequest, User userRequest, Role role) {
		//Set client data
		Client client = new Client();
		BeanUtils.copyProperties(clientRequest, client);
		
		//Set user data
		User user = new User();
		BeanUtils.copyProperties(userRequest, user);
		
		//Relations
		client.setUser(user);
		user.setClient(client);
		user.setRole(role);
		
		return this.clientRepository.save(client);
	}
	
	//DeleteClient
	public void deleteClient(Long id) {
		this.clientRepository.deleteById(id);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
