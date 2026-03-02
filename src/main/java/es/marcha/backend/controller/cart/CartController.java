package es.marcha.backend.controller.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.request.cart.AddCartItemRequestDTO;
import es.marcha.backend.dto.request.cart.UpdateCartItemRequestDTO;
import es.marcha.backend.dto.response.cart.CartResponseDTO;
import es.marcha.backend.services.cart.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Devuelve el carrito activo del usuario.
     * Si no existe carrito activo devuelve 404.
     *
     * @param userId ID del usuario
     * @return {@link CartResponseDTO} con ítems y total
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable long userId) {
        CartResponseDTO cart = cartService.getCartByUserId(userId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    /**
     * Agrega un producto al carrito del usuario.
     * Crea el carrito si no existía. Si el mismo producto+variante ya está,
     * incrementa la cantidad. Valida el stock antes de agregar.
     *
     * @param userId  ID del usuario
     * @param request DTO con productId, variantId (nullable) y quantity
     * @return {@link CartResponseDTO} actualizado con código 201 Created
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @PathVariable long userId,
            @RequestBody AddCartItemRequestDTO request) {
        CartResponseDTO cart = cartService.addItem(userId, request);
        return new ResponseEntity<>(cart, HttpStatus.CREATED);
    }

    /**
     * Actualiza la cantidad de un ítem existente en el carrito.
     * Si quantity == 0, el ítem se elimina automáticamente.
     *
     * @param userId  ID del usuario
     * @param itemId  ID del CartItem
     * @param request DTO con la nueva cantidad
     * @return {@link CartResponseDTO} actualizado
     */
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateItem(
            @PathVariable long userId,
            @PathVariable long itemId,
            @RequestBody UpdateCartItemRequestDTO request) {
        CartResponseDTO cart = cartService.updateItemQuantity(userId, itemId, request);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    /**
     * Elimina un ítem específico del carrito.
     *
     * @param userId ID del usuario
     * @param itemId ID del CartItem a eliminar
     * @return {@link CartResponseDTO} actualizado
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @PathVariable long userId,
            @PathVariable long itemId) {
        CartResponseDTO cart = cartService.removeItem(userId, itemId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    /**
     * Vacía completamente el carrito del usuario (elimina todos los ítems
     * y marca el carrito como CONVERTED).
     * También se llama internamente al confirmar un pedido.
     *
     * @param userId ID del usuario
     * @return 204 No Content
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable long userId) {
        cartService.clearCartByUserId(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
