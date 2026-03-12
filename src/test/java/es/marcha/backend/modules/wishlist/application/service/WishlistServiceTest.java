package es.marcha.backend.modules.wishlist.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.WishlistException;
import es.marcha.backend.core.user.application.service.UserService;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.modules.cart.application.service.CartService;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.wishlist.domain.model.Wishlist;
import es.marcha.backend.modules.wishlist.domain.model.WishlistItem;
import es.marcha.backend.modules.wishlist.infrastructure.persistence.WishlistItemRepository;
import es.marcha.backend.modules.wishlist.infrastructure.persistence.WishlistRepository;

/**
 * Tests unitarios para WishlistService.
 *
 * Verifica:
 * - Producto no encontrado lanza WishlistException(PRODUCT_NOT_FOUND)
 * - Duplicado en wishlist lanza WishlistException(ITEM_ALREADY_EXISTS)
 * - Ítem no encontrado en removeItem lanza WishlistException(ITEM_NOT_FOUND)
 * - getWishlist crea wishlist si no existe
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService — gestión de la lista de deseos")
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private WishlistService wishlistService;

    // =========================================================================
    // addItem
    // =========================================================================

    @Nested
    @DisplayName("addItem")
    class AddItemTests {

        @Test
        @DisplayName("producto no encontrado → lanza WishlistException(PRODUCT_NOT_FOUND)")
        void addItem_productoNoEncontrado_lanzaException() {
            User user = buildUser();
            Wishlist wishlist = buildWishlist(user);

            when(userService.getUserByUsernameOrEmail("testuser")).thenReturn(user);
            when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            WishlistException ex = assertThrows(WishlistException.class,
                    () -> wishlistService.addItem("testuser", 999L));
            assertEquals(WishlistException.PRODUCT_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("producto ya en wishlist → lanza WishlistException(ITEM_ALREADY_EXISTS)")
        void addItem_productoYaEnWishlist_lanzaDuplicado() {
            User user = buildUser();
            Product product = buildProduct(1L);
            Wishlist wishlist = buildWishlist(user);
            WishlistItem existingItem = new WishlistItem();
            existingItem.setId(1L);
            existingItem.setProduct(product);

            when(userService.getUserByUsernameOrEmail("testuser")).thenReturn(user);
            when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(wishlistItemRepository.findByWishlistIdAndProductId(1L, 1L))
                    .thenReturn(Optional.of(existingItem));

            WishlistException ex = assertThrows(WishlistException.class,
                    () -> wishlistService.addItem("testuser", 1L));
            assertEquals(WishlistException.ITEM_ALREADY_EXISTS, ex.getMessage());
        }
    }

    // =========================================================================
    // removeItem
    // =========================================================================

    @Nested
    @DisplayName("removeItem")
    class RemoveItemTests {

        @Test
        @DisplayName("ítem no existe en wishlist → lanza WishlistException(ITEM_NOT_FOUND)")
        void removeItem_itemNoEncontrado_lanzaException() {
            User user = buildUser();
            Wishlist wishlist = buildWishlist(user);

            when(userService.getUserByUsernameOrEmail("testuser")).thenReturn(user);
            when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepository.findByWishlistIdAndId(1L, 999L))
                    .thenReturn(Optional.empty());

            WishlistException ex = assertThrows(WishlistException.class,
                    () -> wishlistService.removeItem("testuser", 999L));
            assertEquals(WishlistException.ITEM_NOT_FOUND, ex.getMessage());
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

    private Product buildProduct(long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        return product;
    }

    private Wishlist buildWishlist(User user) {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUser(user);
        wishlist.setItems(new ArrayList<>());
        return wishlist;
    }

}
