package es.marcha.backend.modules.wishlist.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.modules.wishlist.application.dto.request.AddWishlistItemRequestDTO;
import es.marcha.backend.modules.cart.application.dto.response.CartResponseDTO;
import es.marcha.backend.modules.wishlist.application.dto.response.WishlistResponseDTO;
import es.marcha.backend.modules.wishlist.application.service.WishlistService;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // ─────────────────────────────────────────────────────────────────────────────
    // Endpoints autenticados
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Devuelve la wishlist completa del usuario autenticado.
     * Si el usuario todavía no tiene wishlist, se crea automáticamente.
     *
     * @return {@link WishlistResponseDTO} con los ítems guardados
     */
    @GetMapping
    public ResponseEntity<WishlistResponseDTO> getWishlist() {
        String username = getAuthenticatedUsername();
        WishlistResponseDTO wishlist = wishlistService.getWishlist(username);
        return new ResponseEntity<>(wishlist, HttpStatus.OK);
    }

    /**
     * Agrega un producto a la wishlist del usuario autenticado.
     *
     * @param request DTO con el {@code productId} a agregar
     * @return {@link WishlistResponseDTO} actualizada con código 201 Created
     */
    @PostMapping("/items")
    public ResponseEntity<WishlistResponseDTO> addItem(@RequestBody AddWishlistItemRequestDTO request) {
        String username = getAuthenticatedUsername();
        WishlistResponseDTO wishlist = wishlistService.addItem(username, request.getProductId());
        return new ResponseEntity<>(wishlist, HttpStatus.CREATED);
    }

    /**
     * Elimina un ítem específico de la wishlist del usuario autenticado.
     *
     * @param itemId ID del ítem a eliminar
     * @return {@link WishlistResponseDTO} actualizada
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<WishlistResponseDTO> removeItem(@PathVariable long itemId) {
        String username = getAuthenticatedUsername();
        WishlistResponseDTO wishlist = wishlistService.removeItem(username, itemId);
        return new ResponseEntity<>(wishlist, HttpStatus.OK);
    }

    /**
     * Vacía completamente la wishlist del usuario autenticado.
     *
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {
        String username = getAuthenticatedUsername();
        wishlistService.clearWishlist(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Mueve un ítem de la wishlist al carrito del usuario autenticado.
     * El producto se agrega al carrito con cantidad 1 y se elimina de la wishlist.
     *
     * @param itemId ID del ítem de la wishlist a mover
     * @return {@link CartResponseDTO} actualizado tras agregar el producto
     */
    @PostMapping("/items/{itemId}/move-to-cart")
    public ResponseEntity<CartResponseDTO> moveToCart(@PathVariable long itemId) {
        String username = getAuthenticatedUsername();
        CartResponseDTO cart = wishlistService.moveToCart(username, itemId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Endpoint público (informativo)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Devuelve cuántos usuarios tienen este producto en su wishlist.
     * Endpoint público, sin necesidad de autenticación.
     *
     * @param productId ID del producto
     * @return número de wishlists que contienen el producto
     */
    @GetMapping("/product/{productId}/count")
    public ResponseEntity<Long> countProductInWishlists(@PathVariable long productId) {
        long count = wishlistService.countProductInWishlists(productId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Utilidades internas
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Extrae el username del usuario autenticado desde el
     * {@link SecurityContextHolder}.
     *
     * @return username del usuario autenticado
     */
    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal();
        }
        return auth.getName();
    }
}
