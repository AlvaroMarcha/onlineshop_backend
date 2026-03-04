package es.marcha.backend.core.auth.domain.model;

import java.time.LocalDateTime;

import es.marcha.backend.core.user.domain.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    /**
     * Fecha de revocación explícita (logout).
     * {@code null} mientras el token esté activo.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /** @return {@code true} si el token ha sido revocado o ha expirado. */
    public boolean isExpiredOrRevoked() {
        return revokedAt != null || LocalDateTime.now().isAfter(expiresAt);
    }
}
