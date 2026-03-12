package es.marcha.backend.modules.coupon.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import es.marcha.backend.core.shared.domain.enums.DiscountType;
import es.marcha.backend.modules.coupon.domain.model.Coupon;

/**
 * Tests de repositorio para CouponRepository.
 *
 * Verifica:
 * - existsByCode: retorna true/false según exista el código
 * - existsByCodeAndIdNot: no se considera colisión consigo mismo (update)
 * - findByCode: retorna Optional correcto
 *
 * Usa H2 en memoria. La lógica del service (validación de fechas, etc.)
 * no se testea aquí — está cubierta en CouponServiceTest.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@DisplayName("CouponRepository — consultas custom")
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    private Coupon savedCoupon;

    @BeforeEach
    void setUp() {
        savedCoupon = couponRepository.save(buildCoupon("SAVE10"));
    }

    // =========================================================================
    // existsByCode
    // =========================================================================

    @Nested
    @DisplayName("existsByCode")
    class ExistsByCodeTests {

        @Test
        @DisplayName("código existente → retorna true")
        void existsByCode_codigoExistente_retornaTrue() {
            assertTrue(couponRepository.existsByCode("SAVE10"));
        }

        @Test
        @DisplayName("código no existente → retorna false")
        void existsByCode_codigoNoExistente_retornaFalse() {
            assertFalse(couponRepository.existsByCode("CODIGO_INEXISTENTE"));
        }
    }

    // =========================================================================
    // existsByCodeAndIdNot
    // =========================================================================

    @Nested
    @DisplayName("existsByCodeAndIdNot")
    class ExistsByCodeAndIdNotTests {

        @Test
        @DisplayName("mismo id → retorna false (no hay colisión consigo mismo)")
        void existsByCodeAndIdNot_mismoId_retornaFalse() {
            // Al actualizar un cupón, el propio id se excluye → no debe fallar
            assertFalse(couponRepository.existsByCodeAndIdNot("SAVE10", savedCoupon.getId()));
        }

        @Test
        @DisplayName("id diferente con mismo código → retorna true")
        void existsByCodeAndIdNot_idDiferente_retornaTrue() {
            // Otro cupón distinto existe → detecta duplicado
            Coupon otroCupon = couponRepository.save(buildCoupon("OTRO20"));
            // Al buscar "SAVE10" excluyendo el id de 'otroCupon', el código SAVE10 sí
            // existe
            assertTrue(couponRepository.existsByCodeAndIdNot("SAVE10", otroCupon.getId()));
        }
    }

    // =========================================================================
    // findByCode
    // =========================================================================

    @Nested
    @DisplayName("findByCode")
    class FindByCodeTests {

        @Test
        @DisplayName("código existente → retorna Optional presente con datos correctos")
        void findByCode_codigoExistente_retornaOptional() {
            Optional<Coupon> result = couponRepository.findByCode("SAVE10");
            assertTrue(result.isPresent());
            assertEquals("SAVE10", result.get().getCode());
            assertEquals(DiscountType.PERCENTAGE, result.get().getDiscountType());
        }

        @Test
        @DisplayName("código no existente → retorna Optional vacío")
        void findByCode_codigoNoExistente_retornaVacio() {
            Optional<Coupon> result = couponRepository.findByCode("NOEXISTE");
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Coupon buildCoupon(String code) {
        return Coupon.builder()
                .code(code)
                .discountType(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .validFrom(LocalDate.now())
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
