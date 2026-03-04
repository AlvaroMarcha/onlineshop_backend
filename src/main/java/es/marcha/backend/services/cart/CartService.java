package es.marcha.backend.services.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.cart.AddCartItemRequestDTO;
import es.marcha.backend.dto.request.cart.UpdateCartItemRequestDTO;
import es.marcha.backend.dto.response.cart.CartResponseDTO;
import es.marcha.backend.exception.CartException;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.mapper.cart.CartMapper;
import es.marcha.backend.model.cart.Cart;
import es.marcha.backend.model.cart.CartItem;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.ecommerce.product.ProductVariant;
import es.marcha.backend.model.enums.CartStatus;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.cart.CartItemRepository;
import es.marcha.backend.repository.cart.CartRepository;
import es.marcha.backend.repository.ecommerce.ProductRepository;
import es.marcha.backend.repository.ecommerce.ProductVariantRepository;
import es.marcha.backend.services.user.UserService;
import jakarta.transaction.Transactional;

@Service
public class CartService {

    /** Tiempo de expiración del carrito tras la última modificación */
    private static final int EXPIRY_HOURS = 2;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private UserService userService;

    // ─── Consulta ────────────────────────────────────────────────────────────────

    /**
     * Devuelve el carrito activo del usuario autenticado.
     * Si no existe, se crea automáticamente.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @return {@link CartResponseDTO} con el carrito y sus ítems
     */
    public CartResponseDTO getCartByUsername(String username) {
        User user = userService.getUserByUsernameOrEmail(username);
        Cart cart = getOrCreateActiveCart(user);
        return CartMapper.toCartDTO(cart);
    }

    // ─── Agregar ítem
    // ─────────────────────────────────────────────────────────────

    /**
     * Agrega un producto al carrito del usuario autenticado.
     * <p>
     * Si el carrito no existe se crea automáticamente.
     * Si el mismo producto+variante ya está en el carrito, se incrementa la
     * cantidad en lugar de crear un ítem duplicado.
     * Se valida el stock disponible antes de agregar.
     * </p>
     *
     * @param username nombre de usuario (extraído del JWT)
     * @param request  DTO con productId, variantId (nullable) y quantity
     * @return {@link CartResponseDTO} actualizado
     * @throws ProductException si el producto no existe, está inactivo o no hay
     *                          stock
     * @throws CartException    si la cantidad solicitada no es válida
     */
    @Transactional
    public CartResponseDTO addItem(String username, AddCartItemRequestDTO request) {
        if (request.getQuantity() <= 0)
            throw new CartException(CartException.QUANTITY_INVALID);

        User user = userService.getUserByUsernameOrEmail(username);
        Cart cart = getOrCreateActiveCart(user);

        // Resolver producto y variante
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

        if (!product.isActive())
            throw new ProductException(ProductException.DEFAULT);

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ProductException(ProductException.DEFAULT));
        }

        // Validar stock disponible (variante tiene prioridad sobre producto base)
        int stockDisponible = (variant != null) ? variant.getStock() : product.getStock();
        if (stockDisponible <= 0 || stockDisponible < request.getQuantity())
            throw new ProductException(ProductException.INSUFFICIENT_STOCK);

        // Precio efectivo en el momento de agregar (snapshot)
        BigDecimal unitPrice = resolveUnitPrice(product, variant);

        // Si ya existe el mismo item, incrementar cantidad; si no, crear uno nuevo
        Long variantId = (variant != null) ? variant.getId() : null;
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductIdAndVariantId(cart.getId(), product.getId(), variantId)
                .orElse(null);

        if (existingItem != null) {
            // Verificar que la cantidad total no supera el stock
            int totalQuantity = existingItem.getQuantity() + request.getQuantity();
            if (totalQuantity > stockDisponible)
                throw new ProductException(ProductException.INSUFFICIENT_STOCK);

            existingItem.setQuantity(totalQuantity);
            existingItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Añadir a la lista del carrito para que el mapper lo vea sin recargar
            if (cart.getItems() == null)
                cart.setItems(new ArrayList<>());

            CartItem savedItem = cartItemRepository.save(newItem);
            cart.getItems().add(savedItem);
        }

        // Refrescar expiración: 2 horas desde ahora
        refreshExpiry(cart);
        cartRepository.save(cart);

        return CartMapper.toCartDTO(cart);
    }

    // ─── Actualizar cantidad
    // ──────────────────────────────────────────────────────

    /**
     * Actualiza la cantidad de un ítem existente en el carrito del usuario
     * autenticado.
     * Si la nueva cantidad es 0 o menos, el ítem se elimina.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @param itemId   ID del CartItem a modificar
     * @param request  DTO con la nueva cantidad
     * @return {@link CartResponseDTO} actualizado
     * @throws CartException    si el carrito o el ítem no existen
     * @throws ProductException si la nueva cantidad supera el stock disponible
     */
    @Transactional
    public CartResponseDTO updateItemQuantity(String username, long itemId, UpdateCartItemRequestDTO request) {
        User user = userService.getUserByUsernameOrEmail(username);
        Cart cart = getActiveCartOrThrow(user.getId());
        CartItem item = cartItemRepository.findByCartIdAndId(cart.getId(), itemId)
                .orElseThrow(() -> new CartException(CartException.ITEM_NOT_FOUND));

        // Cantidad <= 0 → eliminar el ítem directamente
        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item);
        } else {
            // Validar stock antes de actualizar
            int stockDisponible = (item.getVariant() != null)
                    ? item.getVariant().getStock()
                    : item.getProduct().getStock();

            if (request.getQuantity() > stockDisponible)
                throw new ProductException(ProductException.INSUFFICIENT_STOCK);

            item.setQuantity(request.getQuantity());
            item.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(item);
        }

        refreshExpiry(cart);
        cartRepository.save(cart);
        // Recargar el carrito desde BD para que el mapper tenga los datos frescos
        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new CartException());
        return CartMapper.toCartDTO(updatedCart);
    }

    // ─── Eliminar ítem
    // ────────────────────────────────────────────────────────────

    /**
     * Elimina un ítem del carrito del usuario autenticado.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @param itemId   ID del CartItem a eliminar
     * @return {@link CartResponseDTO} actualizado
     * @throws CartException si el carrito o el ítem no se encuentran
     */
    @Transactional
    public CartResponseDTO removeItem(String username, long itemId) {
        User user = userService.getUserByUsernameOrEmail(username);
        Cart cart = getActiveCartOrThrow(user.getId());
        CartItem item = cartItemRepository.findByCartIdAndId(cart.getId(), itemId)
                .orElseThrow(() -> new CartException(CartException.ITEM_NOT_FOUND));

        // Remover de la colección - orphanRemoval=true se encarga de eliminar de BD
        cart.getItems().remove(item);
        refreshExpiry(cart);
        cartRepository.save(cart);

        // Recargar para asegurar que el mapper tiene datos frescos
        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new CartException());
        return CartMapper.toCartDTO(updatedCart);
    }

    // ─── Limpiar carrito
    // ──────────────────────────────────────────────────────────

    /**
     * Elimina todos los ítems del carrito activo del usuario autenticado.
     * Se usa externamente al confirmar un pedido.
     *
     * @param username nombre de usuario (extraído del JWT)
     */
    @Transactional
    public void clearCartByUsername(String username) {
        User user = userService.getUserByUsernameOrEmail(username);
        cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    cart.setStatus(CartStatus.CONVERTED);
                    cart.setUpdatedAt(LocalDateTime.now());
                    cartRepository.save(cart);
                });
    }

    // ─── Expiración automática (llamada por ScheduledTaskService)
    // ─────────────────

    /**
     * Marca como EXPIRED todos los carritos activos cuya fecha de expiración
     * ya ha pasado. Llamado periódicamente por
     * {@link es.marcha.backend.services.scheduled.ScheduledTaskService}.
     */
    @Transactional
    public void expireOldCarts() {
        List<Cart> expired = cartRepository.findAllByStatusAndExpiresAtBefore(
                CartStatus.ACTIVE, LocalDateTime.now());

        if (expired.isEmpty())
            return;

        expired.forEach(cart -> {
            cart.setStatus(CartStatus.EXPIRED);
            cart.setUpdatedAt(LocalDateTime.now());
        });

        cartRepository.saveAll(expired);
    }

    // ─── Helpers privados
    // ─────────────────────────────────────────────────────────

    /**
     * Devuelve el carrito activo del usuario o lanza CartException.
     */
    private Cart getActiveCartOrThrow(long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartException(CartException.DEFAULT));
    }

    /**
     * Obtiene el carrito activo del usuario; si no existe, crea uno nuevo.
     */
    private Cart getOrCreateActiveCart(User user) {
        return cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .createdAt(now)
                            .updatedAt(now)
                            .expiresAt(now.plusHours(EXPIRY_HOURS))
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Determina el precio unitario efectivo: si la variante tiene precio propio
     * se usa ese, si tiene descuento en variante también; si no, se usa el
     * precio (o descuento) del producto base.
     */
    private BigDecimal resolveUnitPrice(Product product, ProductVariant variant) {
        if (variant != null) {
            if (variant.getDiscountPriceOverride() != null
                    && variant.getDiscountPriceOverride().compareTo(BigDecimal.ZERO) > 0)
                return variant.getDiscountPriceOverride();
            if (variant.getPriceOverride() != null)
                return variant.getPriceOverride();
        }
        if (product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
            return product.getDiscountPrice();
        return product.getPrice();
    }

    /**
     * Refresca la expiración del carrito: updatedAt = ahora, expiresAt = ahora +
     * 2h.
     */
    private void refreshExpiry(Cart cart) {
        LocalDateTime now = LocalDateTime.now();
        cart.setUpdatedAt(now);
        cart.setExpiresAt(now.plusHours(EXPIRY_HOURS));
    }
}
