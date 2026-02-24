package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.ecommerce.CategoryResponseDTO;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.mapper.CategoryMapper;
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

    public CategoryResponseDTO getCategoryById(long id) {
        CategoryResponseDTO category = catRepository.findById(id)
                .filter(c -> c.isActive())
                .map(CategoryMapper::toCategoryDTO)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_CATEGORY));
        return category;
    }

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

    @Transactional
    public CategoryResponseDTO saveCategory(Category category) {
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setSlug(ProductUtils.createSlug(category.getName().trim()));

        return CategoryMapper.toCategoryDTO(catRepository.save(category));
    }

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

    @Transactional
    public String deleteCategory(long id) {
        Category category = catRepository.findById(id).filter(c -> c.isActive())
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_CATEGORY));
        catRepository.delete(category);
        return CATEGORY_DELETED;
    }

}
