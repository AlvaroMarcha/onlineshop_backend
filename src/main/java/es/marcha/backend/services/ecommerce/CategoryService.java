package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.ecommerce.CategoryResponseDTO;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.mapper.product.CategoryMapper;
import es.marcha.backend.model.ecommerce.Category;
import es.marcha.backend.repository.ecommerce.CategoryRepository;
import es.marcha.backend.utils.ProductUtils;
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
     * @return Lista de {@link CategoryResponseDTO} con todas las categorías activas.
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
     * Inicializa el estado activo, la fecha de creación y genera el slug a partir del nombre.
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
     * @param category La entidad {@link Category} con los nuevos datos. Debe incluir un ID válido.
     * @return {@link CategoryResponseDTO} con los datos actualizados de la categoría.
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
