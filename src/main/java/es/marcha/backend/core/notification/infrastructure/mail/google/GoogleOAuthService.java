package es.marcha.backend.core.notification.infrastructure.mail.google;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleOAuthService {

    /** Segundos de margen antes de la expiración real para considerar el token como obsoleto y pre-refrescarlo. */
    private static final int EXPIRY_BUFFER_SECONDS = 60;

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.refresh-token}")
    private String refreshToken;

    @Value("${app.google.token-uri}")
    private String tokenUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ReentrantLock lock = new ReentrantLock();

    // volatile garantiza que todos los hilos lean siempre los últimos valores escritos
    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;

    /**
     * Devuelve un access token OAuth2 válido para autenticarse con Gmail SMTP.
     * <p>
     * Utiliza un sistema de caché con double-checked locking para minimizar las llamadas
     * al endpoint de Google. Solo lanza una petición HTTP cuando el token está ausente
     * o a punto de expirar (dentro del margen de {@value #EXPIRY_BUFFER_SECONDS} segundos).
     * </p>
     * <p>
     * El método es thread-safe: solo un hilo refresca el token a la vez; el resto espera
     * y reutiliza el token recién obtenido.
     * </p>
     *
     * @return Access token de OAuth2 válido.
     * @throws RuntimeException si no se puede obtener un token de Google.
     */
    public String getAccessToken() {
        // Fast path — no lock needed if the cached token is still valid
        if (isTokenValid()) {
            log.debug("Google OAuth2 — using cached access token (expires at {})", tokenExpiry);
            return cachedAccessToken;
        }

        // Slow path — acquire lock and refresh (double-checked to avoid redundant requests)
        lock.lock();
        try {
            if (isTokenValid()) {
                log.debug("Google OAuth2 — token was refreshed by another thread, reusing it");
                return cachedAccessToken;
            }
            return refreshAccessToken();
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Comprueba si el token en caché sigue siendo válido.
     * Se considera válido si existe y su fecha de expiración (con margen) es posterior al momento actual.
     *
     * @return {@code true} si el token es válido, {@code false} si está ausente o caducado.
     */
    private boolean isTokenValid() {
        return cachedAccessToken != null && Instant.now().isBefore(tokenExpiry);
    }

    /**
     * Solicita un nuevo access token a Google usando el refresh token almacenado.
     * Actualiza el token en caché y su fecha de expiración.
     * <p>
     * Este método solo debe llamarse desde dentro del bloque de bloqueo de {@link #getAccessToken()}.
     * </p>
     *
     * @return El nuevo access token obtenido de Google.
     * @throws RuntimeException si la petición falla o la respuesta está vacía.
     */
    private String refreshAccessToken() {
        log.info("Google OAuth2 — cached token expired or absent, requesting a new one");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenUri, request, TokenResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TokenResponse token = response.getBody();

                cachedAccessToken = token.accessToken();
                tokenExpiry = Instant.now().plusSeconds(token.expiresIn() - EXPIRY_BUFFER_SECONDS);

                log.info("Google OAuth2 — access token refreshed, scope: '{}', valid until: {}",
                        token.scope(), tokenExpiry);

                return cachedAccessToken;
            }
        } catch (Exception e) {
            log.error("Google OAuth2 — error refreshing access token: {}", e.getMessage());
            throw new RuntimeException("Failed to obtain Google OAuth2 access token", e);
        }

        throw new RuntimeException("Failed to obtain Google OAuth2 access token: empty or unexpected response");
    }

    // ------------------------------------------------------------------
    // Internal DTO — maps the JSON response from Google's token endpoint
    // ------------------------------------------------------------------
    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("scope") String scope) {
    }
}
