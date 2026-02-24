package es.marcha.backend.controller.ecommerce;

import java.util.List;

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

import es.marcha.backend.dto.response.ecommerce.CategoryResponseDTO;
import es.marcha.backend.dto.response.ecommerce.SubcategoryResponseDTO;
import es.marcha.backend.model.ecommerce.Category;
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.services.ecommerce.CategoryService;
import es.marcha.backend.services.ecommerce.SubcategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService catService;

    @Autowired
    private SubcategoryService subcatService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = catService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = catService.getCategoryById(id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> saveCategory(@RequestBody Category category) {
        CategoryResponseDTO savedCategory = catService.saveCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<CategoryResponseDTO> updateCategory(@RequestBody Category category) {
        CategoryResponseDTO updatedCategory = catService.updateCategory(category);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        return new ResponseEntity<>(catService.deleteCategory(id), HttpStatus.OK);
    }

    // ** SUBCATEGORIES **

    @PostMapping("/subcategories")
    public ResponseEntity<SubcategoryResponseDTO> createSubcategory(@RequestBody Subcategory subcategory) {
        SubcategoryResponseDTO savedSubcategory = subcatService.createSubcategory(subcategory);
        return new ResponseEntity<>(savedSubcategory, HttpStatus.OK);
    }

    @PutMapping("/subcategories")
    public ResponseEntity<SubcategoryResponseDTO> updateSubcategory(@RequestBody Subcategory subcategory) {
        SubcategoryResponseDTO updatedSubcategory = subcatService.updateSubcategory(subcategory);
        return new ResponseEntity<>(updatedSubcategory, HttpStatus.OK);
    }

    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<String> deleteSubcategory(@PathVariable Long id) {
        return new ResponseEntity<>(subcatService.deleteSubcategory(id), HttpStatus.OK);
    }
}
