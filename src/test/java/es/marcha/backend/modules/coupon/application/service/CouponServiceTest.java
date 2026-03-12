package es.marcha.backend.modules.coupon.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.CouponException;
import es.marcha.backend.modules.coupon.application.dto.request.CouponRequestDTO;
import es.marcha.backend.modules.coupon.domain.model.Coupon;
import es.marcha.backend.core.shared.domain.enums.DiscountType;
import es.marcha.backend.modules.coupon.infrastructure.persistence.CouponRepository;
import es.marcha.backend.modules.coupon.infrastructure.persistence.CouponUserUsageRepository;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;

/**
 * Tests unitarios para CouponService.
 *
 * Verifica:
 * - getCouponById lanza CouponException si no existe
 * - createCoupon lanza CouponException si el código ya existe
 * - validateCoupon lanza CouponException para cupones inactivos, caducados,
 * sin stock o con importe mínimo no alcanzado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService — validación y gestión de cupones")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserUsageRepository usageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;

    // =========================================================================
    // getCouponById
    // =========================================================================

    @Nested
    @DisplayName("getCouponById")
    class GetCouponByIdTests {

        @Test
        @DisplayName("no existe → lanza CouponException")
        void getCouponById_noExiste_lanzaException() {
            when(couponRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(CouponException.class, () -> couponService.getCouponById(99L));
        }

        @Test
        @DisplayName("existe → devuelve DTO")
        void getCouponById_existe_devuelveDTO() {
            Coupon coupon = buildCoupon("SAVE10", true, null, null);
            when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
            assertDoesNotThrow(() -> couponService.getCouponById(1L));
        }
    }

    // =========================================================================
    // createCoupon
    // =========================================================================

    @Nested
    @DisplayName("createCoupon")
    class CreateCouponTests {

        @Test
        @DisplayName("código ya existe → lanza CouponException(CODE_ALREADY_EXISTS)")
        void createCoupon_codigoExistente_lanzaException() {
            when(couponRepository.existsByCode("SAVE10")).thenReturn(true);

            CouponRequestDTO dto = buildCouponRequest("save10"); // minúsculas → normalizado a SAVE10
            CouponException ex = assertThrows(CouponException.class,
                    () -> couponService.createCoupon(dto));
            assertEquals(CouponException.CODE_ALREADY_EXISTS, ex.getMessage());
        }
    }

    // =========================================================================
    // validateCoupon
    // =========================================================================

    @Nested
    @DisplayName("validateCoupon")
    class ValidateCouponTests {

        @Test
        @DisplayName("cupón no encontrado por código → lanza CouponException")
        void validateCoupon_codigoNoEncontrado_lanzaException() {
            when(couponRepository.findByCode("NOEXISTE")).thenReturn(Optional.empty());
            assertThrows(CouponException.class,
                    () -> couponService.validateCoupon("NOEXISTE", BigDecimal.TEN, 1L));
        }

        @Test
        @DisplayName("cupón inactivo → lanza CouponException(INACTIVE)")
        void validateCoupon_inactivo_lanzaException() {
            Coupon coupon = buildCoupon("INACTIVE10", false, null, null);
            when(couponRepository.findByCode("INACTIVE10")).thenReturn(Optional.of(coupon));

            CouponException ex = assertThrows(CouponException.class,
                    () -> couponService.validateCoupon("INACTIVE10", BigDecimal.TEN, 1L));
            assertEquals(CouponException.INACTIVE, ex.getMessage());
        }

        @Test
        @DisplayName("cupón caducado → lanza CouponException(EXPIRED)")
        void validateCoupon_caducado_lanzaException() {
            Coupon coupon = buildCoupon("OLD10", true,
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(1)); // expirado ayer
            when(couponRepository.findByCode("OLD10")).thenReturn(Optional.of(coupon));

            CouponException ex = assertThrows(CouponException.class,
                    () -> couponService.validateCoupon("OLD10", BigDecimal.TEN, 1L));
            assertEquals(CouponException.EXPIRED, ex.getMessage());
        }

        @Test
        @DisplayName("cupón no válido aún → lanza CouponException(NOT_YET_VALID)")
        void validateCoupon_noValidoAun_lanzaException() {
            Coupon coupon = buildCoupon("FUTURE10", true,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(30)); // válido desde mañana
            when(couponRepository.findByCode("FUTURE10")).thenReturn(Optional.of(coupon));

            CouponException ex = assertThrows(CouponException.class,
                    () -> couponService.validateCoupon("FUTURE10", BigDecimal.TEN, 1L));
            assertEquals(CouponException.NOT_YET_VALID, ex.getMessage());
        }

        @Test
        @DisplayName("importe mínimo no alcanzado → lanza CouponException(MIN_AMOUNT_NOT_MET)")
        void validateCoupon_importeMinimoNoAlcanzado_lanzaException() {
            Coupon coupon = buildCoupon("MIN50", true, null, null);
            coupon.setMinOrderAmount(new BigDecimal("50.00")); // mínimo 50€
            when(couponRepository.findByCode("MIN50")).thenReturn(Optional.of(coupon));

            // Solo pedimos con 30€, no alcanza el mínimo
            CouponException ex = assertThrows(CouponException.class,
                    () -> couponService.validateCoupon("MIN50", new BigDecimal("30.00"), 1L));
            assertEquals(CouponException.MIN_AMOUNT_NOT_MET, ex.getMessage());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Coupon buildCoupon(String code, boolean active,
            LocalDate validFrom, LocalDate validUntil) {
        return Coupon.builder()
                .id(1L)
                .code(code)
                .isActive(active)
                .discountType(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .minOrderAmount(BigDecimal.ZERO)
                .validFrom(validFrom != null ? validFrom : LocalDate.now().minusDays(1))
                .validUntil(validUntil != null ? validUntil : LocalDate.now().plusDays(30))
                .maxUses(null)
                .maxUsesPerUser(null)
                .createdAt(LocalDateTime.now())
                .applicableToUsers(List.of())
                .build();
    }

    private CouponRequestDTO buildCouponRequest(String code) {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setCode(code);
        dto.setDiscountType(DiscountType.PERCENTAGE);
        dto.setValue(new BigDecimal("10.00"));
        dto.setActive(true);
        return dto;
    }
}
