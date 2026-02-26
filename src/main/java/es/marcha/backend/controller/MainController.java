package es.marcha.backend.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {
	
	/**
	 * Verifica el estado de salud de la API.
	 *
	 * @return {@link ResponseEntity} con un mapa que incluye el timestamp actual y el estado {@code UP},
	 *         con código HTTP 200 OK.
	 */
	@GetMapping("/health/status")
	public ResponseEntity<Map<String, Object>> checkingHealth() throws Exception {
		Map<String, Object> body = Map.of(
				"timestamp", LocalDateTime.now().toString(),
				"status", "UP"
				
				);
		return ResponseEntity.ok(body);
	}


	/**
	 * Endpoint público accesible sin autenticación.
	 *
	 * @return Mensaje de bienvenida para cualquier usuario.
	 */
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
