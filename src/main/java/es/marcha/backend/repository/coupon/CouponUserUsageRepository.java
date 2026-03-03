package es.marcha.backend.repository.coupon;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.coupon.CouponUserUsage;

@Repository
public interface CouponUserUsageRepository extends JpaRepository<CouponUserUsage, Long> {

    /** Devuelve el registro de uso de un cupón para un usuario concreto. */
    Optional<CouponUserUsage> findByCouponIdAndUserId(long couponId, long userId);

    /**
     * Número total de usos de un cupón por un usuario concreto (0 si nunca lo ha
     * usado).
     */
    default int getUsageCountByUser(long couponId, long userId) {
        return findByCouponIdAndUserId(couponId, userId)
                .map(CouponUserUsage::getUsageCount)
                .orElse(0);
    }
}
