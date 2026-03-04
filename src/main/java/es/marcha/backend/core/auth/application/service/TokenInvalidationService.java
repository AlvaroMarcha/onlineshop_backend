package es.marcha.backend.core.auth.application.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Servicio en memoria que gestiona la invalidación inmediata de JWT.
 * <p>
 * Cuando un SUPER_ADMIN cambia el rol de un usuario o lo banea, su username
 * se añade al conjunto de invalidados. El {@code JwtFilter} rechaza cualquier
 * token cuyo subject (username) esté en este conjunto con 401 Unauthorized.
 * </p>
 * <p>
 * Al hacer login, el username se elimina del conjunto para permitir la
 * emisión de un nuevo JWT con el rol actualizado.
 * </p>
 * <p>
 * Nota: el conjunto es en memoria. Un reinicio del servidor lo limpia,
 * pero los JWT tienen TTL propio y expirarán de forma natural.
 * </p>
 */
@Service
public class TokenInvalidationService {

    // Conjunto thread-safe de usernames con JWT invalidado
    private final Set<String> invalidatedUsernames = ConcurrentHashMap.newKeySet();

    /**
     * Marca el JWT del usuario como invalidado.
     * Cualquier petición con ese username será rechazada hasta que vuelva a hacer
     * login.
     *
     * @param username el username del usuario afectado
     */
    public void invalidate(String username) {
        invalidatedUsernames.add(username);
    }

    /**
     * Comprueba si el JWT del usuario está invalidado.
     *
     * @param username el username a comprobar
     * @return {@code true} si el JWT está invalidado
     */
    public boolean isInvalidated(String username) {
        return invalidatedUsernames.contains(username);
    }

    /**
     * Elimina la invalidación del usuario, permitiéndole usar su nuevo JWT.
     * Se llama desde {@code AuthService.login()} tras un login exitoso.
     *
     * @param username el username del usuario
     */
    public void clearInvalidation(String username) {
        invalidatedUsernames.remove(username);
    }
}
