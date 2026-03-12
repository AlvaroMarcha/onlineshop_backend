package es.marcha.backend.modules.coupon.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.core.user.application.service.UserService;
import es.marcha.backend.modules.coupon.application.dto.response.CouponResponseDTO;
import es.marcha.backend.modules.coupon.application.service.CouponService;

/**
 * Tests de capa web para CouponController.
 *
 * Verifica:
 * - GET /coupons → 200 OK con lista completa
 * - GET /coupons/{id} → 200 OK cuando existe
 * - POST /coupons → 201 CREATED
 * - DELETE /coupons/{id} → 204 NO CONTENT
 */
@WebMvcTest(value = CouponController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("CouponController — CRUD de cupones")
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @MockBean
    private UserService userService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /coupons
    // =========================================================================

    @Nested
    @DisplayName("GET /coupons")
    class GetAllCouponsTests {

        @Test
        @DisplayName("devuelve 200 con lista de cupones")
        void getAllCoupons_devuelve200() throws Exception {
            CouponResponseDTO coupon = buildCouponResponse(1L, "SAVE10");
            when(couponService.getAllCoupons()).thenReturn(List.of(coupon));

            mockMvc.perform(get("/coupons").with(user("admin")))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // GET /coupons/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /coupons/{id}")
    class GetCouponByIdTests {

        @Test
        @DisplayName("cupón existe → 200 OK")
        void getCouponById_existe_devuelve200() throws Exception {
            when(couponService.getCouponById(1L)).thenReturn(buildCouponResponse(1L, "SAVE10"));

            mockMvc.perform(get("/coupons/1").with(user("admin")))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // POST /coupons
    // =========================================================================

    @Nested
    @DisplayName("POST /coupons")
    class CreateCouponTests {

        @Test
        @DisplayName("crea cupón válido → 201 CREATED")
        void createCoupon_peticionValida_devuelve201() throws Exception {
            CouponResponseDTO response = buildCouponResponse(1L, "NEWCOUPON");
            when(couponService.createCoupon(any())).thenReturn(response);

            String body = """
                    {
                        "code": "NEWCOUPON",
                        "discountType": "PERCENTAGE",
                        "value": 10,
                        "validFrom": "2026-01-01",
                        "validUntil": "2026-12-31",
                        "isActive": true
                    }
                    """;

            mockMvc.perform(post("/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(user("admin")))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // DELETE /coupons/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /coupons/{id}")
    class DeleteCouponTests {

        @Test
        @DisplayName("elimina cupón → 204 NO CONTENT")
        void deleteCoupon_devuelve204() throws Exception {
            doNothing().when(couponService).deleteCoupon(1L);

            mockMvc.perform(delete("/coupons/1").with(user("admin")))
                    .andExpect(status().isNoContent());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CouponResponseDTO buildCouponResponse(Long id, String code) {
        CouponResponseDTO dto = new CouponResponseDTO();
        dto.setId(id);
        dto.setCode(code);
        return dto;
    }
}
