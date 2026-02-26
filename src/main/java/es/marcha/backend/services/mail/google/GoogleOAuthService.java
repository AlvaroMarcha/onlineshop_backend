package es.marcha.backend.services.mail.google;

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

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.refresh-token}")
    private String refreshToken;

    @Value("${app.google.token-uri}")
    private String tokenUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Exchanges the stored refresh token for a fresh access token.
     * The refresh token never expires (unless revoked), so this is safe to call
     * on every send operation.
     *
     * @return valid OAuth2 Bearer access token
     */
    public String getAccessToken() {
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
                log.info("Google OAuth2 access token refreshed — scope: '{}', expires_in: {}s",
                        token.scope(), token.expiresIn());
                return token.accessToken();
            }
        } catch (Exception e) {
            log.error("Error refreshing Google OAuth2 access token: {}", e.getMessage());
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
