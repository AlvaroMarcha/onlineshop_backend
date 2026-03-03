package es.marcha.backend.controller.coupon;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.request.coupon.CouponRequestDTO;
import es.marcha.backend.dto.response.coupon.CouponResponseDTO;
import es.marcha.backend.dto.response.coupon.CouponValidationResponseDTO;
import es.marcha.backend.services.coupon.CouponService;
import es.marcha.backend.services.user.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    // ─────────────────────────────────────────────────────────────────────────────
    // Endpoints de administración (ROLE_ADMIN / ROLE_SUPER_ADMIN)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Devuelve todos los cupones del sistema (activos e inactivos).
     *
     * @return lista de {@link CouponResponseDTO}
     */
    @GetMapping
    public ResponseEntity<List<CouponResponseDTO>> getAllCoupons() {
        return new ResponseEntity<>(couponService.getAllCoupons(), HttpStatus.OK);
    }

    /**
     * Devuelve un cupón por su ID.
     *
     * @param id ID del cupón
     * @return {@link CouponResponseDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> getCouponById(@PathVariable long id) {
        return new ResponseEntity<>(couponService.getCouponById(id), HttpStatus.OK);
    }

    /**
     * Crea un nuevo cupón.
     *
     * @param dto datos del cupón a crear
     * @return {@link CouponResponseDTO} del cupón creado con código 201 Created
     */
    @PostMapping
    public ResponseEntity<CouponResponseDTO> createCoupon(@Valid @RequestBody CouponRequestDTO dto) {
        return new ResponseEntity<>(couponService.createCoupon(dto), HttpStatus.CREATED);
    }

    /**
     * Actualiza un cupón existente.
     *
     * @param id  ID del cupón a actualizar
     * @param dto nuevos datos del cupón
     * @return {@link CouponResponseDTO} actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable long id,
            @Valid @RequestBody CouponRequestDTO dto) {
        return new ResponseEntity<>(couponService.updateCoupon(id, dto), HttpStatus.OK);
    }

    /**
     * Elimina un cupón por su ID.
     * Para cupones ya usados en pedidos se recomienda desactivarlos vía PUT en
     * lugar de eliminiarlos.
     *
     * @param id ID del cupón a eliminar
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable long id) {
        couponService.deleteCoupon(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Endpoint público de validación
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Valida un código de cupón y calcula el descuento aplicable para el importe
     * de pedido indicado.
     * <p>
     * Endpoint público — no requiere autenticación. Sin embargo, si el usuario
     * está autenticado, su ID se extrae del token para validar cupones de usuario
     * específico.
     * </p>
     *
     * @param code        código del cupón
     * @param orderAmount importe bruto del pedido (base imponible, sin IVA)
     * @return {@link CouponValidationResponseDTO} con el descuento calculado
     */
    @GetMapping("/{code}/validate")
    public ResponseEntity<CouponValidationResponseDTO> validateCoupon(
            @PathVariable String code,
            @RequestParam BigDecimal orderAmount) {

        // Intentar extraer userId del contexto de seguridad (si el usuario está
        // autenticado)
        Long userId = resolveUserIdFromContext();

        CouponValidationResponseDTO result = couponService.validateCoupon(code, orderAmount, userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Utilidades internas
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Intenta resolver el ID del usuario autenticado desde el
     * SecurityContextHolder.
     * Si no hay usuario autenticado, devuelve {@code null}.
     *
     * @return ID del usuario autenticado o {@code null}
     */
    private Long resolveUserIdFromContext() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String username = (String) auth.getPrincipal();
            return userService.getUserByUsernameOrEmail(username).getId();
        } catch (Exception e) {
            return null;
        }
    }
}
