package es.marcha.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import es.marcha.backend.model.User;
import es.marcha.backend.security.JwtUtil;
import es.marcha.backend.services.ClientService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    //Attribs
    @Autowired
    private ClientService clientService;

    //TESTING...
	@PostMapping("/login")
    public String login(@RequestParam String user, @RequestParam String pass) {
        User existUser = clientService.getClientByUsername(user);
        return existUser !=null && existUser.getPassword().equals(pass) ? JwtUtil.generarToken(user) : null;
    }
}
