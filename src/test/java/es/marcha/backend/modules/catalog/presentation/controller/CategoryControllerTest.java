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

import com.fasterxml.jackson.databind.ObjectMapper;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.modules.catalog.application.dto.response.CategoryResponseDTO;
import es.marcha.backend.modules.catalog.application.service.CategoryService;
import es.marcha.backend.modules.catalog.application.service.SubcategoryService;
import es.marcha.backend.modules.catalog.domain.model.Category;

/**
 * Tests de capa web para CategoryController.
 *
 * Verifica:
 * - GET /categories → 200 OK con lista
 * - GET /categories/{id} → 200 OK cuando existe / 500 cuando no existe
 * - POST /categories → 201 CREATED
 */
@WebMvcTest(value = CategoryController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("CategoryController — CRUD de categorías")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService catService;

    @MockBean
    private SubcategoryService subcatService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /categories
    // =========================================================================

    @Nested
    @DisplayName("GET /categories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("devuelve 200 con lista de categorías")
        void getAllCategories_devuelve200() throws Exception {
            CategoryResponseDTO cat = new CategoryResponseDTO();
            cat.setId(1L);
            cat.setName("Ropa");
            when(catService.getAllCategories()).thenReturn(List.of(cat));

            mockMvc.perform(get("/categories"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // GET /categories/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /categories/{id}")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("categoría existe → 200 OK")
        void getCategoryById_existe_devuelve200() throws Exception {
            CategoryResponseDTO cat = new CategoryResponseDTO();
            cat.setId(1L);
            when(catService.getCategoryById(1L)).thenReturn(cat);

            mockMvc.perform(get("/categories/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("categoría no existe → GlobalExceptionHandler → 500")
        void getCategoryById_noExiste_devuelve500() throws Exception {
            when(catService.getCategoryById(99L))
                    .thenThrow(new ProductException(ProductException.FAILED_FETCH_CATEGORY));

            mockMvc.perform(get("/categories/99"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================================
    // POST /categories
    // =========================================================================

    @Nested
    @DisplayName("POST /categories")
    class CreateCategoryTests {

        @Test
        @DisplayName("crea categoría válida → 201 CREATED")
        void createCategory_peticionValida_devuelve201() throws Exception {
            Category input = new Category();
            input.setName("Electrónica");

            CategoryResponseDTO response = new CategoryResponseDTO();
            response.setId(1L);
            response.setName("Electrónica");
            when(catService.saveCategory(any(Category.class))).thenReturn(response);

            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated());
        }
    }
}
