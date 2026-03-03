package es.marcha.backend.services.coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.dto.request.coupon.CouponRequestDTO;
import es.marcha.backend.dto.response.coupon.CouponResponseDTO;
import es.marcha.backend.dto.response.coupon.CouponValidationResponseDTO;
import es.marcha.backend.exception.CouponException;
import es.marcha.backend.model.coupon.Coupon;
import es.marcha.backend.model.coupon.CouponUserUsage;
import es.marcha.backend.model.enums.DiscountType;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.coupon.CouponRepository;
import es.marcha.backend.repository.coupon.CouponUserUsageRepository;
import es.marcha.backend.repository.user.UserRepository;

@Service
public class CouponService {

    private static final int SCALE = 2;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserUsageRepository usageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Devuelve todos los cupones del sistema (activos e inactivos).
     *
     * @return lista de {@link CouponResponseDTO}
     */
    @Transactional(readOnly = true)
    public List<CouponResponseDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Devuelve un cupón por su ID.
     *
     * @param id ID del cupón
     * @return {@link CouponResponseDTO}
     * @throws CouponException si no existe
     */
    @Transactional(readOnly = true)
    public CouponResponseDTO getCouponById(long id) {
        return toResponseDTO(findByIdOrThrow(id));
    }

    /**
     * Crea un nuevo cupón.
     * El código se normaliza a mayúsculas para evitar duplicados por
     * capitalización.
     *
     * @param dto datos del nuevo cupón
     * @return {@link CouponResponseDTO} del cupón creado
     * @throws CouponException si ya existe un cupón con ese código
     */
    @Transactional
    public CouponResponseDTO createCoupon(CouponRequestDTO dto) {
        String normalizedCode = dto.getCode().toUpperCase().trim();

        if (couponRepository.existsByCode(normalizedCode))
            throw new CouponException(CouponException.CODE_ALREADY_EXISTS);

        List<User> users = resolveUsers(dto.getApplicableUserIds());

        Coupon coupon = Coupon.builder()
                .code(normalizedCode)
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .value(dto.getValue())
                .minOrderAmount(dto.getMinOrderAmount())
                .maxUses(dto.getMaxUses())
                .maxUsesPerUser(dto.getMaxUsesPerUser())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .isActive(dto.isActive())
                .applicableToUsers(users)
                .createdAt(LocalDateTime.now())
                .build();

        Coupon saved = couponRepository.save(coupon);
        couponRepository.flush();
        return toResponseDTO(couponRepository.findById(saved.getId()).orElse(saved));
    }

    /**
     * Actualiza un cupón existente.
     * El código se normaliza a mayúsculas.
     * Si se cambia el código se verifica que no exista ya otro cupón con el nuevo
     * código.
     *
     * @param id  ID del cupón a actualizar
     * @param dto nuevos datos
     * @return {@link CouponResponseDTO} actualizado
     * @throws CouponException si no existe o el nuevo código ya está en uso
     */
    @Transactional
    public CouponResponseDTO updateCoupon(long id, CouponRequestDTO dto) {
        Coupon coupon = findByIdOrThrow(id);
        String normalizedCode = dto.getCode().toUpperCase().trim();

        if (couponRepository.existsByCodeAndIdNot(normalizedCode, id))
            throw new CouponException(CouponException.CODE_ALREADY_EXISTS);

        List<User> users = resolveUsers(dto.getApplicableUserIds());

        coupon.setCode(normalizedCode);
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setValue(dto.getValue());
        coupon.setMinOrderAmount(dto.getMinOrderAmount());
        coupon.setMaxUses(dto.getMaxUses());
        coupon.setMaxUsesPerUser(dto.getMaxUsesPerUser());
        coupon.setValidFrom(dto.getValidFrom());
        coupon.setValidUntil(dto.getValidUntil());
        coupon.setActive(dto.isActive());
        coupon.setApplicableToUsers(users);
        coupon.setUpdatedAt(LocalDateTime.now());

        Coupon saved = couponRepository.save(coupon);
        couponRepository.flush();
        return toResponseDTO(couponRepository.findById(saved.getId()).orElse(saved));
    }

    /**
     * Elimina físicamente un cupón por su ID.
     * Solo para cupones que no tengan pedidos asociados; en caso contrario,
     * se recomienda desactivarlo con {@link #updateCoupon}.
     *
     * @param id ID del cupón a eliminar
     * @throws CouponException si no existe
     */
    @Transactional
    public void deleteCoupon(long id) {
        Coupon coupon = findByIdOrThrow(id);
        couponRepository.delete(coupon);
    }

    /**
     * Valida un código de cupón y calcula el descuento aplicable para un importe
     * de pedido dado.
     * <p>
     * No modifica el cupón (no incrementa usedCount). Solo informa del descuento.
     * </p>
     *
     * @param code        código del cupón (insensible a mayúsculas)
     * @param orderAmount importe bruto del pedido (base imponible) sobre el que se
     *                    aplica
     * @param userId      ID del usuario que intenta usar el cupón (null si no está
     *                    autenticado)
     * @return {@link CouponValidationResponseDTO} con el descuento calculado
     * @throws CouponException con mensaje descriptivo si la validación falla
     */
    public CouponValidationResponseDTO validateCoupon(String code, BigDecimal orderAmount, Long userId) {
        Coupon coupon = findAndValidate(code, orderAmount, userId);
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount).max(BigDecimal.ZERO)
                .setScale(SCALE, RoundingMode.HALF_UP);

        return CouponValidationResponseDTO.builder()
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getValue())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message("Cupón válido. Se aplicará un descuento de " + discountAmount + " € en tu pedido.")
                .build();
    }

    /**
     * Valida el cupón y devuelve la entidad para que {@code OrderService} pueda
     * guardar el couponId en la orden.
     * Lanza excepciones descriptivas si la validación falla.
     *
     * @param code        código del cupón
     * @param orderAmount importe bruto del pedido (base imponible)
     * @param userId      ID del usuario que realiza el pedido
     * @return entidad {@link Coupon} validada y lista para usarse
     */
    public Coupon findAndValidate(String code, BigDecimal orderAmount, Long userId) {
        String normalizedCode = code.toUpperCase().trim();
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new CouponException(CouponException.DEFAULT));

        LocalDate today = LocalDate.now();

        // 1. Verificar que el cupón está activo
        if (!coupon.isActive())
            throw new CouponException(CouponException.INACTIVE);

        // 2. Verificar fechas de validez
        if (today.isBefore(coupon.getValidFrom()))
            throw new CouponException(CouponException.NOT_YET_VALID);
        if (today.isAfter(coupon.getValidUntil()))
            throw new CouponException(CouponException.EXPIRED);

        // 3. Verificar número máximo de usos
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses())
            throw new CouponException(CouponException.MAX_USES_REACHED);

        // 4. Verificar importe mínimo del pedido
        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0)
            throw new CouponException(CouponException.MIN_AMOUNT_NOT_MET);

        // 5. Verificar si el cupón está restringido a usuarios concretos
        List<User> allowed = coupon.getApplicableToUsers();
        if (allowed != null && !allowed.isEmpty()) {
            if (userId == null)
                throw new CouponException(CouponException.NOT_APPLICABLE_TO_USER);
            boolean userAllowed = allowed.stream().anyMatch(u -> u.getId() == userId);
            if (!userAllowed)
                throw new CouponException(CouponException.NOT_APPLICABLE_TO_USER);
        }

        // 6. Verificar límite de usos por usuario
        if (coupon.getMaxUsesPerUser() != null && userId != null) {
            int userUsageCount = usageRepository.getUsageCountByUser(coupon.getId(), userId);
            if (userUsageCount >= coupon.getMaxUsesPerUser())
                throw new CouponException(CouponException.MAX_USES_REACHED);
        }

        return coupon;
    }

    /**
     * Calcula el importe de descuento a aplicar sobre la base imponible del pedido.
     *
     * @param coupon      cupón validado
     * @param orderAmount importe bruto del pedido (base imponible)
     * @return importe de descuento (nunca negativo, nunca mayor que orderAmount)
     */
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            // Descuento porcentual sobre la base imponible
            discount = orderAmount.multiply(coupon.getValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        } else {
            // Descuento fijo — no puede superar el importe del pedido
            discount = coupon.getValue().min(orderAmount);
        }
        return discount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Incrementa el contador de usos global del cupón y registra (o actualiza)
     * el uso individual del usuario.
     * Se llama desde {@code OrderService} después de crear la orden con éxito.
     *
     * @param couponId ID del cupón que se ha usado
     * @param userId   ID del usuario que lo ha usado
     */
    @Transactional
    public void incrementUsedCount(long couponId, long userId) {
        // Incrementar contador global
        Coupon coupon = findByIdOrThrow(couponId);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponRepository.save(coupon);

        // Registrar o actualizar uso por usuario
        LocalDateTime now = LocalDateTime.now();
        usageRepository.findByCouponIdAndUserId(couponId, userId)
                .ifPresentOrElse(
                        usage -> {
                            usage.setUsageCount(usage.getUsageCount() + 1);
                            usage.setLastUsedAt(now);
                            usageRepository.save(usage);
                        },
                        () -> usageRepository.save(CouponUserUsage.builder()
                                .coupon(coupon)
                                .user(userRepository.getReferenceById(userId))
                                .usageCount(1)
                                .firstUsedAt(now)
                                .lastUsedAt(now)
                                .build()));
    }

    /**
     * Convierte una entidad {@link Coupon} en su DTO de respuesta.
     */
    public CouponResponseDTO toResponseDTO(Coupon coupon) {
        List<Long> userIds = coupon.getApplicableToUsers() != null
                ? coupon.getApplicableToUsers().stream().map(User::getId).toList()
                : List.of();

        return CouponResponseDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .value(coupon.getValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxUses(coupon.getMaxUses())
                .maxUsesPerUser(coupon.getMaxUsesPerUser())
                .usedCount(coupon.getUsedCount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .isActive(coupon.isActive())
                .applicableUserIds(userIds)
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    private Coupon findByIdOrThrow(long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponException(CouponException.DEFAULT));
    }

    /**
     * Resuelve una lista de IDs de usuario a entidades {@link User}.
     * Se ignoran silenciosamente los IDs que no existan.
     */
    private List<User> resolveUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        return new ArrayList<>(userRepository.findAllById(ids));
    }
}
