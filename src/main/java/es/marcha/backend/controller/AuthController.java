package es.marcha.backend.controller;

import org.apache.catalina.connector.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import es.marcha.backend.dto.request.CreateClientDTO;
import es.marcha.backend.dto.request.LoginRequestDTO;
import es.marcha.backend.dto.request.RequestClientDTO;
import es.marcha.backend.dto.request.RequestUserDTO;
import es.marcha.backend.dto.response.ResponseAuthDTO;
import es.marcha.backend.dto.response.ResponseUserDTO;
import es.marcha.backend.model.Client;
import es.marcha.backend.model.Role;
import es.marcha.backend.model.User;
import es.marcha.backend.security.JwtUtil;
import es.marcha.backend.services.ClientService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
public class AuthController {
    //Attribs
    @Autowired
    private ClientService clientService;

    //Return user and token by Login 
	@PostMapping("/login")
    public ResponseEntity<ResponseAuthDTO> login(@RequestBody LoginRequestDTO loginRequest){ 
        User existUser = clientService.getClientByUsername(loginRequest.getUser());
        ResponseUserDTO userDTO = new ResponseUserDTO(
            existUser.getName(),
            existUser.getUsername(),
            existUser.getEmail(),
            existUser.getPhone(),
            existUser.isStatus(),
            existUser.getEmail_verified_at(),
            existUser.isLocked(),
            existUser.getLast_login_at(),
            existUser.getCreated_at(),
            existUser.getRole().getId()
        );
        String token = null;
        if (existUser != null && existUser.getPassword().equals(loginRequest.getPass())){
            token = JwtUtil.generarToken(loginRequest.getUser());
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ResponseAuthDTO response = new ResponseAuthDTO(token, userDTO, HttpStatus.OK.toString());
        return new ResponseEntity<ResponseAuthDTO>(response, HttpStatus.OK);
    }

     //Return user and token by Login 
	@PostMapping("/register")
    public ResponseEntity<ResponseAuthDTO> register(@RequestBody CreateClientDTO clientRequest){ 
        User existUser = clientService.getClientByUsername(clientRequest.getUser().getUsername());

        if(existUser != null){
           return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseAuthDTO(null, null, "User already exist"));
        }
        //New data for new user
        RequestClientDTO newClient = new RequestClientDTO(); 
        BeanUtils.copyProperties(clientRequest.getClient(), newClient);
        RequestUserDTO newUser = new RequestUserDTO();
        BeanUtils.copyProperties(clientRequest.getUser(), newUser);
        Role roleUser = new Role();
        BeanUtils.copyProperties(clientRequest.getRole(), roleUser);

        //Saving Client with user
        Client clientSaved = this.clientService.createClientWithUser(newClient, newUser, roleUser);
        String token = JwtUtil.generarToken(clientRequest.getUser().getUsername());
        ResponseUserDTO userDTO = new ResponseUserDTO(
            clientSaved.getUser().getName(),
            clientSaved.getUser().getUsername(),
            clientSaved.getUser().getEmail(),
            clientSaved.getUser().getPhone(),
            clientSaved.getUser().isStatus(),
            clientSaved.getUser().getEmail_verified_at(),
            clientSaved.getUser().isLocked(),
            clientSaved.getUser().getLast_login_at(),
            clientSaved.getUser().getCreated_at(),
            clientSaved.getUser().getRole().getId()
        );

        ResponseAuthDTO responseDTO = new ResponseAuthDTO(token, userDTO, HttpStatus.OK.toString());
        return new ResponseEntity<ResponseAuthDTO>(responseDTO, HttpStatus.OK);
    }



}
