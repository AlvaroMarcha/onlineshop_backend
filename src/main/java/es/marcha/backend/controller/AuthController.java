package es.marcha.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    	//TESTING...
	@PostMapping("/login")
    public String login(@RequestParam String user, @RequestParam String pass) {
		System.out.println("LOGIN - Usuario: " + user + " - Pass: " + pass);
        // Aquí validarías contra BD o lo que uses
        if ("alan".equals(user) && "1234".equals(pass)) {
            return JwtUtil.generarToken(user);
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }

   
    
}
