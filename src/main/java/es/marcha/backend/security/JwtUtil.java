package es.marcha.backend.security;

import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static SecretKey SECRET_KEY;
    /** Duración del access token en milisegundos (por defecto 60 min). */
    private static long ACCESS_TOKEN_EXPIRATION_MS;

    /**
     * Inicializa la clave secreta HMAC-SHA y la duración del access token
     * a partir de los valores configurados en {@code application.properties}.
     *
     * @param secret         Clave secreta en formato base64.
     * @param expirationMs   Duración del access token en milisegundos.
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiration-ms:3600000}") long expirationMs) {
        SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        ACCESS_TOKEN_EXPIRATION_MS = expirationMs;
    }

    /**
     * Genera un token JWT firmado para el usuario indicado, incluyendo su rol.
     * El token tiene una validez de 1 hora desde el momento de su generación.
     *
     * @param username El nombre de usuario que se incluye como {@code subject}.
     * @param roleName El nombre del rol (p. ej. {@code ROLE_USER}), almacenado como
     *                 claim {@code role}.
     * @return Token JWT compacto y firmado.
     */
    public static String generateToken(String username, String roleName) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", roleName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Valida un token JWT y extrae el {@code subject} (username) contenido en él.
     * Si el token es inválido, ha expirado o está mal formado, lanza una excepción.
     *
     * @param token El token JWT a validar.
     * @return El {@code subject} del token, que corresponde al username del
     *         usuario.
     * @throws io.jsonwebtoken.JwtException Si el token no es válido o ha expirado.
     */
    public static String validateToken(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token)
                .getBody().getSubject();
    }

    /**
     * Extrae el nombre del rol almacenado en el claim {@code role} del token JWT.
     *
     * @param token El token JWT del que se extrae el rol.
     * @return El nombre del rol (p. ej. {@code ROLE_USER}), o {@code null} si el
     *         claim no existe.
     */
    public static String getRoleFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token)
                .getBody().get("role", String.class);
    }

}
