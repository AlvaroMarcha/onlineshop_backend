package es.marcha.backend.modules.catalog.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.catalog.application.dto.response.CategoryResponseDTO;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.application.mapper.CategoryMapper;
import es.marcha.backend.modules.catalog.domain.model.Category;
import es.marcha.backend.modules.catalog.infrastructure.persistence.CategoryRepository;
import es.marcha.backend.core.shared.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class CategoryService {
    // Attribs
    @Autowired
    private CategoryRepository catRepository;

    public static final String CATEGORY_DELETED = "CATEGORY WAS DELETED";

    /**
     * Obtiene una categoría activa por su ID.
     *
     * @param id El ID de la categoría a buscar.
     * @return {@link CategoryResponseDTO} con los datos de la categoría.
     * @throws ProductException si la categoría no existe o no está activa.
     */
    public CategoryResponseDTO getCategoryById(long id) {
        CategoryResponseDTO category = catRepository.findById(id)
                .filter(c -> c.isActive())
                .map(CategoryMapper::toCategoryDTO)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_CATEGORY));
        return category;
    }

    /**
     * Obtiene todas las categorías activas del sistema.
     *
     * @return Lista de {@link CategoryResponseDTO} con todas las categorías
     *         activas.
     * @throws ProductException si no hay ninguna categoría activa.
     */
    public List<CategoryResponseDTO> getAllCategories() {
        List<CategoryResponseDTO> categories = catRepository.findAll().stream()
                .filter(c -> c.isActive())
                .map(CategoryMapper::toCategoryDTO)
                .toList();
        if (categories.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_CATEGORY);
        }

        return categories;
    }

    /**
     * Crea y persiste una nueva categoría en el sistema.
     * Inicializa el estado activo, la fecha de creación y genera el slug a partir
     * del nombre.
     *
     * @param category La entidad {@link Category} a crear.
     * @return {@link CategoryResponseDTO} con los datos de la categoría creada.
     */
    @Transactional
    public CategoryResponseDTO saveCategory(Category category) {
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setSlug(ProductUtils.createSlug(category.getName().trim()));

        return CategoryMapper.toCategoryDTO(catRepository.save(category));
    }

    /**
     * Actualiza el nombre, descripción y slug de una categoría existente.
     *
     * @param category La entidad {@link Category} con los nuevos datos. Debe
     *                 incluir un ID válido.
     * @return {@link CategoryResponseDTO} con los datos actualizados de la
     *         categoría.
     * @throws ProductException si la categoría no existe o no está activa.
     */
    @Transactional
    public CategoryResponseDTO updateCategory(Category category) {
        Category existingCategory = catRepository.findById(category.getId())
                .filter(c -> c.isActive())
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_CATEGORY));

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setSlug(ProductUtils.createSlug(category.getName().trim()));
        existingCategory.setUpdatedAt(LocalDateTime.now());

        return CategoryMapper.toCategoryDTO(catRepository.save(existingCategory));
    }

    /**
     * Obtiene una lista de categorías a partir de una lista de IDs.
     * <p>
     * Este método consulta el repositorio y devuelve las entidades completas
     * de Category correspondientes a los IDs proporcionados. Si no se encuentra
     * ninguna categoría, lanza una excepción.
     *
     * @param categoryIds Lista de IDs de categorías a buscar.
     * @return Lista de categorías correspondientes a los IDs.
     * @throws ProductException Si no se encuentra ninguna categoría.
     */
    public List<Category> getAllCategoriesHandler(List<Long> categoryIds) {
        // Lista nula o vacía es válida: el producto se crea sin categorías directas
        if (categoryIds == null || categoryIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<Category> categories = catRepository.findAllById(categoryIds);

        if (categories.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_CATEGORY);
        }
        return categories;
    }

    /**
     * Elimina una categoría activa de la base de datos por su ID.
     *
     * @param id El ID de la categoría a eliminar.
     * @return Mensaje de confirmación de la eliminación.
     * @throws ProductException si la categoría no existe o no está activa.
     */
    @Transactional
    public String deleteCategory(long id) {
        Category category = catRepository.findById(id).filter(c -> c.isActive())
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_CATEGORY));
        catRepository.delete(category);
        return CATEGORY_DELETED;
    }

}
