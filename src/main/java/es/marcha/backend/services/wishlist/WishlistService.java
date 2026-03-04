package es.marcha.backend.services.wishlist;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.cart.AddCartItemRequestDTO;
import es.marcha.backend.dto.response.cart.CartResponseDTO;
import es.marcha.backend.dto.response.wishlist.WishlistResponseDTO;
import es.marcha.backend.exception.WishlistException;
import es.marcha.backend.mapper.wishlist.WishlistMapper;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.user.User;
import es.marcha.backend.model.wishlist.Wishlist;
import es.marcha.backend.model.wishlist.WishlistItem;
import es.marcha.backend.repository.ecommerce.ProductRepository;
import es.marcha.backend.repository.wishlist.WishlistItemRepository;
import es.marcha.backend.repository.wishlist.WishlistRepository;
import es.marcha.backend.services.cart.CartService;
import es.marcha.backend.services.user.UserService;
import jakarta.transaction.Transactional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    // ─── Consulta ────────────────────────────────────────────────────────────────

    /**
     * Devuelve la wishlist del usuario autenticado.
     * Si el usuario aún no tiene wishlist, se crea automáticamente.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @return {@link WishlistResponseDTO} con los ítems guardados
     */
    public WishlistResponseDTO getWishlist(String username) {
        User user = userService.getUserByUsernameOrEmail(username);
        Wishlist wishlist = getOrCreateWishlist(user);
        return WishlistMapper.toWishlistDTO(wishlist);
    }

    /**
     * Devuelve el conjunto de IDs de productos que el usuario tiene en su wishlist.
     * Se usa en el {@code ProductController} para calcular el campo
     * {@code isInWishlist} de cada {@code ProductResponseDTO}.
     * Si el usuario no tiene wishlist, devuelve un conjunto vacío.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @return {@link Set} de IDs de productos en la wishlist del usuario
     */
    public Set<Long> getWishlistProductIds(String username) {
        return wishlistRepository.findByUserUsername(username)
                .map(wishlist -> wishlist.getItems().stream()
                        .map(item -> item.getProduct().getId())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    // ─── Agregar ítem
    // ─────────────────────────────────────────────────────────────

    /**
     * Agrega un producto a la wishlist del usuario.
     * Si la wishlist no existe, se crea. Si el producto ya está en la wishlist,
     * lanza una excepción para evitar duplicados.
     *
     * @param username  nombre de usuario (extraído del JWT)
     * @param productId ID del producto a agregar
     * @return {@link WishlistResponseDTO} actualizada
     * @throws WishlistException si el producto ya existe en la wishlist o no se
     *                           encuentra
     */
    @Transactional
    public WishlistResponseDTO addItem(String username, long productId) {
        User user = userService.getUserByUsernameOrEmail(username);
        Wishlist wishlist = getOrCreateWishlist(user);

        // Verificar que el producto existe
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new WishlistException(WishlistException.PRODUCT_NOT_FOUND));

        // Verificar que el producto no está ya en la wishlist
        wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId)
                .ifPresent(existing -> {
                    throw new WishlistException(WishlistException.ITEM_ALREADY_EXISTS);
                });

        WishlistItem newItem = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .addedAt(LocalDateTime.now())
                .build();

        WishlistItem savedItem = wishlistItemRepository.save(newItem);
        wishlist.getItems().add(savedItem);

        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);

        return WishlistMapper.toWishlistDTO(wishlist);
    }

    // ─── Eliminar ítem
    // ────────────────────────────────────────────────────────────

    /**
     * Elimina un ítem específico de la wishlist del usuario.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @param itemId   ID del {@link WishlistItem} a eliminar
     * @return {@link WishlistResponseDTO} actualizada
     * @throws WishlistException si el ítem no pertenece a la wishlist del usuario
     */
    @Transactional
    public WishlistResponseDTO removeItem(String username, long itemId) {
        Wishlist wishlist = getWishlistOrThrow(username);

        WishlistItem item = wishlistItemRepository.findByWishlistIdAndId(wishlist.getId(), itemId)
                .orElseThrow(() -> new WishlistException(WishlistException.ITEM_NOT_FOUND));

        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);

        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);

        // Recargar desde BD para devolver el estado actualizado
        Wishlist updated = wishlistRepository.findById(wishlist.getId())
                .orElseThrow(() -> new WishlistException());
        return WishlistMapper.toWishlistDTO(updated);
    }

    // ─── Vaciar wishlist
    // ──────────────────────────────────────────────────────────

    /**
     * Vacía completamente la wishlist del usuario, eliminando todos sus ítems.
     *
     * @param username nombre de usuario (extraído del JWT)
     */
    @Transactional
    public void clearWishlist(String username) {
        Wishlist wishlist = getWishlistOrThrow(username);
        wishlist.getItems().clear();
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);
    }

    // ─── Mover al carrito
    // ─────────────────────────────────────────────────────────

    /**
     * Mueve un ítem de la wishlist al carrito del usuario.
     * Agrega el producto al carrito con cantidad 1 y elimina el ítem de la
     * wishlist.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @param itemId   ID del {@link WishlistItem} a mover
     * @return {@link CartResponseDTO} actualizado tras agregar el producto
     * @throws WishlistException si el ítem no pertenece a la wishlist del usuario
     */
    @Transactional
    public CartResponseDTO moveToCart(String username, long itemId) {
        Wishlist wishlist = getWishlistOrThrow(username);

        WishlistItem item = wishlistItemRepository.findByWishlistIdAndId(wishlist.getId(), itemId)
                .orElseThrow(() -> new WishlistException(WishlistException.ITEM_NOT_FOUND));

        // Construir la petición para agregar al carrito
        AddCartItemRequestDTO cartRequest = AddCartItemRequestDTO.builder()
                .productId(item.getProduct().getId())
                .quantity(1)
                .build();

        // Agregar al carrito usando el servicio existente
        CartResponseDTO cartResponse = cartService.addItem(username, cartRequest);

        // Eliminar el ítem de la wishlist después de moverlo al carrito
        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);

        return cartResponse;
    }

    // ─── Endpoint público
    // ─────────────────────────────────────────────────────────

    /**
     * Devuelve cuántos usuarios tienen este producto en su wishlist.
     * Este dato es público e informativo.
     *
     * @param productId ID del producto
     * @return número de wishlists que contienen el producto
     */
    public long countProductInWishlists(long productId) {
        return wishlistItemRepository.countByProductId(productId);
    }

    // ─── Métodos internos
    // ─────────────────────────────────────────────────────────

    /**
     * Obtiene la wishlist del usuario o crea una nueva si no existe todavía.
     * La wishlist persiste entre sesiones al tener respaldo en base de datos.
     *
     * @param user entidad de usuario
     * @return wishlist existente o recién creada
     */
    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
    }

    /**
     * Obtiene la wishlist existente del usuario o lanza una excepción si no existe.
     *
     * @param username nombre de usuario (extraído del JWT)
     * @return la wishlist del usuario
     * @throws WishlistException si el usuario no tiene ninguna wishlist creada
     */
    private Wishlist getWishlistOrThrow(String username) {
        User user = userService.getUserByUsernameOrEmail(username);
        return wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new WishlistException(WishlistException.DEFAULT));
    }
}
