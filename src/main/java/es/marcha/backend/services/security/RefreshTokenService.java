package es.marcha.backend.services.security;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.security.RefreshToken;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.security.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration-days:30}")
    private long refreshTokenExpirationDays;

    /**
     * Genera y persiste un nuevo refresh token para el usuario indicado.
     * Los tokens anteriores activos del usuario siguen siendo válidos hasta
     * que expiren o se revoquen. Se emite uno nuevo en cada login.
     *
     * @param user usuario para el que se genera el token
     * @return entidad {@link RefreshToken} persistida
     */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revokedAt(null)
                .build();
        return refreshTokenRepository.save(token);
    }

    /**
     * Valida el refresh token recibido: comprueba que exista, que no haya
     * expirado y que no haya sido revocado.
     *
     * @param tokenValue valor UUID del refresh token
     * @return entidad {@link RefreshToken} válida
     * @throws UserException con código {@code REFRESH_TOKEN_INVALID} si no existe
     * @throws UserException con código {@code REFRESH_TOKEN_EXPIRED} si ha caducado
     */
    public RefreshToken validateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new UserException(UserException.REFRESH_TOKEN_INVALID));

        if (token.isExpiredOrRevoked()) {
            throw new UserException(UserException.REFRESH_TOKEN_EXPIRED);
        }
        return token;
    }

    /**
     * Revoca todos los refresh tokens activos del usuario (se llama en logout).
     *
     * @param user usuario cuyos tokens se revocan
     */
    public void revokeAllTokensByUser(User user) {
        refreshTokenRepository.revokeAllByUser(user, LocalDateTime.now());
    }

    /**
     * Elimina los tokens expirados y revocados de la base de datos.
     * Se invoca de forma periódica desde
     * {@link es.marcha.backend.services.scheduled.ScheduledTaskService}.
     */
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
    }
}
