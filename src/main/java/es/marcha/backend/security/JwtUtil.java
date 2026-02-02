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

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

    }


    public static String generateToken(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY).compact();
    }

    public static String validateToken(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token)
                .getBody().getSubject();
    }

}
