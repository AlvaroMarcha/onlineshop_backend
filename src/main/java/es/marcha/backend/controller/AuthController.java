package es.marcha.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.request.LoginRequestDTO;
import es.marcha.backend.dto.request.RegisterRequestDTO;
import es.marcha.backend.dto.response.AuthResponseDTO;
import es.marcha.backend.services.security.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // Attribs
    @Autowired
    private AuthService aService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO credentials) {
        AuthResponseDTO response = aService.login(credentials);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO userData) {
        AuthResponseDTO response = aService.register(userData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
