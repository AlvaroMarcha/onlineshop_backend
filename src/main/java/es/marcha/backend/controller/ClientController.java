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

import es.marcha.backend.model.Client;
import es.marcha.backend.services.ClientService;

@RestController
@RequestMapping("api/")
public class ClientController {
	//Attribs
	@Autowired
	private ClientService clientService;
	
	@GetMapping("/clients")
	public ResponseEntity<List<Client>> getAllClients(){
		List<Client> allClients = this.clientService.getAllClients();
		return new ResponseEntity<List<Client>>(allClients, HttpStatus.OK);
	}
	
	@GetMapping("/clients/{id}")
	public ResponseEntity<Client> getClientById(@PathVariable Long id){
		Optional<Client> clientOpt = this.clientService.getClientById(id);
		Client client = null;
		ResponseEntity<Client> response = new ResponseEntity<Client>(HttpStatus.NOT_FOUND);
		if(clientOpt.isPresent()) {
			client = clientOpt.get();
			response = new ResponseEntity<Client>(client, HttpStatus.OK);
		}
		return response;
		
	}
	
	
}
