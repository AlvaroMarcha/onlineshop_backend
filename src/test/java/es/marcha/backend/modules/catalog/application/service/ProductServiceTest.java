package es.marcha.backend.modules.catalog.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.infrastructure.persistence.InventoryRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;

/**
 * Tests unitarios para ProductService.
 *
 * Verifica:
 * - Producto activo y no eliminado → devuelve DTO
 * - Producto eliminado → lanza ProductException
 * - Producto inactivo → lanza ProductException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService — obtención de productos")
class ProductServiceTest {

    @Mock
    private ProductRepository prodRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductReviewService prService;

    @Mock
    private SubcategoryService subcategoryService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductImageService imageService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductService productService;

    // =========================================================================
    // getProductById
    // =========================================================================

    @Nested
    @DisplayName("getProductById")
    class GetProductByIdTests {

        @Test
        @DisplayName("producto activo y no eliminado → no lanza excepción")
        void getProductById_activoYNoEliminado_devuelveDTO() {
            Product product = buildProduct(1L, true, false);
            when(prodRepository.findById(1L)).thenReturn(Optional.of(product));

            assertDoesNotThrow(() -> productService.getProductById(1L));
        }

        @Test
        @DisplayName("producto eliminado → lanza ProductException")
        void getProductById_eliminado_lanzaException() {
            Product product = buildProduct(1L, true, true); // deleted=true
            when(prodRepository.findById(1L)).thenReturn(Optional.of(product));

            assertThrows(ProductException.class, () -> productService.getProductById(1L));
        }

        @Test
        @DisplayName("producto inactivo → lanza ProductException")
        void getProductById_inactivo_lanzaException() {
            Product product = buildProduct(1L, false, false); // active=false
            when(prodRepository.findById(1L)).thenReturn(Optional.of(product));

            assertThrows(ProductException.class, () -> productService.getProductById(1L));
        }

        @Test
        @DisplayName("producto no encontrado → lanza ProductException")
        void getProductById_noEncontrado_lanzaException() {
            when(prodRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ProductException.class, () -> productService.getProductById(99L));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Product buildProduct(long id, boolean active, boolean deleted) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setActive(active);
        product.setDeleted(deleted);
        product.setRating(0.0);
        product.setRatingCount(0.0);
        return product;
    }
}
