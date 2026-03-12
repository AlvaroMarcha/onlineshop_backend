package es.marcha.backend.modules.cart.presentation.controller;

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
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.modules.cart.application.dto.request.AddCartItemRequestDTO;
import es.marcha.backend.modules.cart.application.dto.request.UpdateCartItemRequestDTO;
import es.marcha.backend.modules.cart.application.dto.response.CartResponseDTO;
import es.marcha.backend.modules.cart.application.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Devuelve el carrito activo del usuario autenticado.
     * Si no existe carrito activo, se crea automáticamente.
     *
     * @return {@link CartResponseDTO} con ítems y total
     */
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        String username = getAuthenticatedUsername();
        CartResponseDTO cart = cartService.getCartByUsername(username);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    /**
     * Agrega un producto al carrito del usuario autenticado.
     * Crea el carrito si no existía. Si el mismo producto+variante ya está,
     * incrementa la cantidad. Valida el stock antes de agregar.
     *
     * @param request DTO con productId, variantId (nullable) y quantity
     * @return {@link CartResponseDTO} actualizado con código 201 Created
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @RequestBody AddCartItemRequestDTO request) {
        String username = getAuthenticatedUsername();
        CartResponseDTO cart = cartService.addItem(username, request);
        return new ResponseEntity<>(cart, HttpStatus.CREATED);
    }

    /**
     * Actualiza la cantidad de un ítem existente en el carrito del usuario
     * autenticado.
     * Si quantity == 0, el ítem se elimina automáticamente.
     *
     * @param itemId  ID del CartItem
     * @param request DTO con la nueva cantidad
     * @return {@link CartResponseDTO} actualizado
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateItem(
            @PathVariable long itemId,
            @RequestBody UpdateCartItemRequestDTO request) {
        String username = getAuthenticatedUsername();
        CartResponseDTO cart = cartService.updateItemQuantity(username, itemId, request);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    /**
     * Elimina un ítem específico del carrito del usuario autenticado.
     *
     * @param itemId ID del CartItem a eliminar
     * @return {@link CartResponseDTO} actualizado
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @PathVariable long itemId) {
        String username = getAuthenticatedUsername();
        CartResponseDTO cart = cartService.removeItem(username, itemId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    /**
     * Vacía completamente el carrito del usuario autenticado (elimina todos los
     * ítems
     * y marca el carrito como CONVERTED).
     * También se llama internamente al confirmar un pedido.
     *
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        String username = getAuthenticatedUsername();
        cartService.clearCartByUsername(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Extrae el username del usuario autenticado desde el SecurityContextHolder.
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
