package es.marcha.backend.modules.catalog.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.modules.catalog.application.dto.response.product.ProductResponseDTO;
import es.marcha.backend.modules.catalog.application.service.CategoryService;
import es.marcha.backend.modules.catalog.application.service.ProductImageService;
import es.marcha.backend.modules.catalog.application.service.ProductReviewService;
import es.marcha.backend.modules.catalog.application.service.ProductService;
import es.marcha.backend.modules.catalog.application.service.SubcategoryService;
import es.marcha.backend.modules.wishlist.application.service.WishlistService;

/**
 * Tests de capa web para ProductController.
 *
 * Verifica:
 * - GET /products → 200 OK con lista de productos activos
 * - GET /products/{id} → 200 OK cuando producto existe / 500 cuando no existe
 * - POST /products → 201 CREATED
 * - DELETE /products/{id} → 200 OK con mensaje de confirmación
 */
@WebMvcTest(value = ProductController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("ProductController — CRUD de productos")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService prodService;

    @MockBean
    private ProductReviewService rService;

    @MockBean
    private SubcategoryService subcatService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private ProductImageService imageService;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /products
    // =========================================================================

    @Nested
    @DisplayName("GET /products")
    class GetAllProductsTests {

        @Test
        @DisplayName("devuelve 200 con lista de productos activos")
        void getAllProducts_devuelve200() throws Exception {
            ProductResponseDTO producto = new ProductResponseDTO();
            producto.setId(1L);
            producto.setName("Camiseta");
            when(prodService.getAllProducts()).thenReturn(List.of(producto));

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("sin productos → 200 con lista vacía")
        void getAllProducts_sinProductos_devuelve200() throws Exception {
            when(prodService.getAllProducts()).thenReturn(List.of());

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // GET /products/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("producto existe y está activo → 200 OK")
        void getProductById_existe_devuelve200() throws Exception {
            ProductResponseDTO producto = new ProductResponseDTO();
            producto.setId(1L);
            when(prodService.getProductById(1L)).thenReturn(producto);

            mockMvc.perform(get("/products/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("producto no existe → GlobalExceptionHandler → 500")
        void getProductById_noExiste_devuelve500() throws Exception {
            when(prodService.getProductById(99L))
                    .thenThrow(new ProductException(ProductException.FAILED_FETCH));

            mockMvc.perform(get("/products/99"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================================
    // POST /products
    // =========================================================================

    @Nested
    @DisplayName("POST /products")
    class CreateProductTests {

        @Test
        @DisplayName("crea producto → 201 CREATED")
        void createProduct_peticionValida_devuelve201() throws Exception {
            ProductResponseDTO response = new ProductResponseDTO();
            response.setId(1L);
            when(prodService.createProduct(any())).thenReturn(response);
            when(subcatService.getAllSubcategoriesHandler(any())).thenReturn(List.of());
            when(categoryService.getAllCategoriesHandler(any())).thenReturn(List.of());

            String body = """
                    {
                        "name": "Nuevo Producto",
                        "price": 29.99,
                        "subcategoryIds": [],
                        "categoryIds": []
                    }
                    """;

            mockMvc.perform(post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // DELETE /products/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("elimina producto (lógicamente) → 200 OK")
        void deleteProduct_devuelve200() throws Exception {
            when(prodService.deleteProduct(1L)).thenReturn("Producto eliminado correctamente");

            mockMvc.perform(delete("/products/1"))
                    .andExpect(status().isOk());
        }
    }
}
