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

    /**
     * Inicializa la clave secreta HMAC-SHA a partir del valor base64 configurado en
     * {@code jwt.secret}.
     *
     * @param secret Valor de la clave secreta en formato base64, inyectado desde
     *               {@code application.properties}.
     */
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

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
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
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
