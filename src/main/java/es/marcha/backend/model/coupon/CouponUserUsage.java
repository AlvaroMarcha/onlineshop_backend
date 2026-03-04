package es.marcha.backend.model.coupon;

import java.time.LocalDateTime;

import es.marcha.backend.core.user.domain.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "coupon_user_usages", uniqueConstraints = @UniqueConstraint(name = "uk_coupon_user", columnNames = {
        "coupon_id", "user_id" }))
public class CouponUserUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Builder.Default
    @Column(name = "usage_count", nullable = false)
    private int usageCount = 1;
    @Column(name = "first_used_at", nullable = false)
    private LocalDateTime firstUsedAt;
    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;
}
