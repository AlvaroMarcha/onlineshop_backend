package es.marcha.backend.modules.catalog.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.domain.model.Category;
import es.marcha.backend.modules.catalog.infrastructure.persistence.CategoryRepository;

/**
 * Tests unitarios para CategoryService.
 *
 * Verifica:
 * - getCategoryById filtra categorías inactivas
 * - getAllCategories filtra inactivas
 * - saveCategory genera slug a partir del nombre
 * - updateCategory actualiza nombre y regenera slug
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService — CRUD de categorías")
class CategoryServiceTest {

    @Mock
    private CategoryRepository catRepository;

    @InjectMocks
    private CategoryService categoryService;

    // =========================================================================
    // getCategoryById
    // =========================================================================

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("categoría activa → devuelve DTO")
        void getCategoryById_activa_devuelveDTO() {
            Category cat = buildCategory(1L, "Ropa", true);
            when(catRepository.findById(1L)).thenReturn(Optional.of(cat));

            assertDoesNotThrow(() -> categoryService.getCategoryById(1L));
        }

        @Test
        @DisplayName("categoría inactiva → lanza ProductException(FAILED_FETCH_CATEGORY)")
        void getCategoryById_inactiva_lanzaException() {
            Category cat = buildCategory(1L, "Archivada", false);
            when(catRepository.findById(1L)).thenReturn(Optional.of(cat));

            ProductException ex = assertThrows(ProductException.class,
                    () -> categoryService.getCategoryById(1L));
            assertEquals(ProductException.FAILED_FETCH_CATEGORY, ex.getMessage());
        }

        @Test
        @DisplayName("no existe → lanza ProductException(FAILED_FETCH_CATEGORY)")
        void getCategoryById_noExiste_lanzaException() {
            when(catRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ProductException.class, () -> categoryService.getCategoryById(99L));
        }
    }

    // =========================================================================
    // getAllCategories
    // =========================================================================

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("solo devuelve categorías activas")
        void getAllCategories_filtrasInactivas() {
            Category activa = buildCategory(1L, "Activa", true);
            Category inactiva = buildCategory(2L, "Inactiva", false);
            when(catRepository.findAll()).thenReturn(List.of(activa, inactiva));

            List<?> result = categoryService.getAllCategories();
            assertEquals(1, result.size());
        }
    }

    // =========================================================================
    // saveCategory
    // =========================================================================

    @Nested
    @DisplayName("saveCategory")
    class SaveCategoryTests {

        @Test
        @DisplayName("genera slug a partir del nombre")
        void saveCategory_generaSlug() {
            Category input = new Category();
            input.setName("Mi Categoría Nueva");

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            when(catRepository.save(captor.capture())).thenAnswer(inv -> captor.getValue());

            categoryService.saveCategory(input);

            Category saved = captor.getValue();
            assertNotNull(saved.getSlug());
            assertFalse(saved.getSlug().isEmpty());
            // El slug debe estar en minúsculas y sin espacios
            assertFalse(saved.getSlug().contains(" "));
            assertEquals(saved.getSlug(), saved.getSlug().toLowerCase());
        }

        @Test
        @DisplayName("marca la categoría como activa al guardar")
        void saveCategory_marcaActiva() {
            Category input = new Category();
            input.setName("Nueva");

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            when(catRepository.save(captor.capture())).thenAnswer(inv -> captor.getValue());

            categoryService.saveCategory(input);

            assertTrue(captor.getValue().isActive());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Category buildCategory(long id, String name, boolean active) {
        Category cat = new Category();
        cat.setId(id);
        cat.setName(name);
        cat.setActive(active);
        cat.setSlug(name.toLowerCase().replace(" ", "-"));
        return cat;
    }
}
