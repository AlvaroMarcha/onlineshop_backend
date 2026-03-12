package es.marcha.backend.modules.cart.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.CartException;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.core.user.application.service.UserService;
import es.marcha.backend.modules.cart.application.dto.request.AddCartItemRequestDTO;
import es.marcha.backend.modules.cart.domain.enums.CartStatus;
import es.marcha.backend.modules.cart.domain.model.Cart;
import es.marcha.backend.modules.cart.infrastructure.persistence.CartItemRepository;
import es.marcha.backend.modules.cart.infrastructure.persistence.CartRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductVariantRepository;
import es.marcha.backend.core.user.domain.model.User;

/**
 * Tests unitarios para CartService.
 *
 * Verifica:
 * - Validaciones de cantidad en addItem
 * - Producto no encontrado lanza ProductException
 * - Carrito creado automáticamente si no existe (getOrCreate)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService — gestión del carrito")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartService cartService;

    // =========================================================================
    // addItem — validaciones
    // =========================================================================

    @Nested
    @DisplayName("addItem — validaciones de entrada")
    class AddItemValidations {

        @Test
        @DisplayName("cantidad cero → lanza CartException(QUANTITY_INVALID)")
        void addItem_cantidadCero_lanzaCartException() {
            AddCartItemRequestDTO request = new AddCartItemRequestDTO();
            request.setProductId(1L);
            request.setQuantity(0);

            CartException ex = assertThrows(CartException.class,
                    () -> cartService.addItem("testuser", request));
            assertEquals(CartException.QUANTITY_INVALID, ex.getMessage());
        }

        @Test
        @DisplayName("cantidad negativa → lanza CartException(QUANTITY_INVALID)")
        void addItem_cantidadNegativa_lanzaCartException() {
            AddCartItemRequestDTO request = new AddCartItemRequestDTO();
            request.setProductId(1L);
            request.setQuantity(-5);

            assertThrows(CartException.class,
                    () -> cartService.addItem("testuser", request));
        }

        @Test
        @DisplayName("producto no encontrado → lanza ProductException")
        void addItem_productoNoEncontrado_lanzaProductException() {
            User user = buildUser();
            CartStatus active = CartStatus.ACTIVE;
            Cart cart = buildCart(user, active);

            when(userService.getUserByUsernameOrEmail("testuser")).thenReturn(user);
            when(cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            AddCartItemRequestDTO request = new AddCartItemRequestDTO();
            request.setProductId(999L);
            request.setQuantity(1);

            assertThrows(ProductException.class,
                    () -> cartService.addItem("testuser", request));
        }
    }

    // =========================================================================
    // getCartByUsername
    // =========================================================================

    @Nested
    @DisplayName("getCartByUsername")
    class GetCartTests {

        @Test
        @DisplayName("no existe carrito activo → crea uno nuevo y lo devuelve")
        void getCartByUsername_noExisteCarrito_creaUno() {
            User user = buildUser();
            Cart newCart = buildCart(user, CartStatus.ACTIVE);

            when(userService.getUserByUsernameOrEmail("testuser")).thenReturn(user);
            when(cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE))
                    .thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

            assertDoesNotThrow(() -> cartService.getCartByUsername("testuser"));
            verify(cartRepository).save(any(Cart.class));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        return user;
    }

    private Cart buildCart(User user, CartStatus status) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setStatus(status);
        return cart;
    }
}
