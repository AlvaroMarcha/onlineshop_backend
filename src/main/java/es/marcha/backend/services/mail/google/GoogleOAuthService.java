package es.marcha.backend.services.mail.google;

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

    /** Seconds before real expiry to consider the token stale and pre-refresh it. */
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

    // volatile ensures all threads see the latest written values immediately
    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;

    /**
     * Returns a valid OAuth2 access token, hitting Google's token endpoint only
     * when the cached token is absent or about to expire (within {@value #EXPIRY_BUFFER_SECONDS}s).
     *
     * Thread-safe: only one thread refreshes at a time; others wait and then reuse
     * the freshly obtained token.
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

    private boolean isTokenValid() {
        return cachedAccessToken != null && Instant.now().isBefore(tokenExpiry);
    }

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
