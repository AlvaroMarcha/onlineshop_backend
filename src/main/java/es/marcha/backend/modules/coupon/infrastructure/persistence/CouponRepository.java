package es.marcha.backend.modules.coupon.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.coupon.domain.model.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * Busca un cupón por su código (case-insensitive, normalizado a mayúsculas en
     * el servicio)
     */
    Optional<Coupon> findByCode(String code);

    /** Comprueba si ya existe un cupón con ese código */
    boolean existsByCode(String code);

    /**
     * Comprueba si existe otro cupón con ese código excluyendo el que se está editando.
     * Usado en {@code updateCoupon} para permitir guardar sin cambiar el código.
     */
    boolean existsByCodeAndIdNot(String code, long id);
}
