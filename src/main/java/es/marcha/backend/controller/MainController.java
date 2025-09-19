package es.marcha.backend.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {
	
	@GetMapping("/health/status")
	public ResponseEntity<Map<String, Object>> checkingHealth() throws Exception {
		Map<String, Object> body = Map.of(
				"timestamp", LocalDateTime.now().toString(),
				"status", "UP"
				
				);
		return ResponseEntity.ok(body);
	}


	@GetMapping("/publico")
    public String publico() {
        return "Cualquiera puede ver esto";
    }

    @GetMapping("/privado")
    public String privado(Authentication authentication) {
        String user = (String) authentication.getPrincipal();
        return "Hola " + user + ", estás en la zona privada";
    }




}
