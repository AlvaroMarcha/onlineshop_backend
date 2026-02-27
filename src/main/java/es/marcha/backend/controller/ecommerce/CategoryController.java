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

    /**
     * Obtiene todas las categorías activas del sistema.
     *
     * @return {@link ResponseEntity} con la lista de {@link CategoryResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = catService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    /**
     * Obtiene una categoría activa por su ID.
     *
     * @param id El ID de la categoría a obtener.
     * @return {@link ResponseEntity} con el {@link CategoryResponseDTO} correspondiente y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = catService.getCategoryById(id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    /**
     * Crea y persiste una nueva categoría en el sistema.
     *
     * @param category La entidad {@link Category} a crear.
     * @return {@link ResponseEntity} con el {@link CategoryResponseDTO} creado y código HTTP 201 CREATED.
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> saveCategory(@RequestBody Category category) {
        CategoryResponseDTO savedCategory = catService.saveCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    /**
     * Actualiza una categoría existente con los nuevos datos proporcionados.
     *
     * @param category La entidad {@link Category} con los datos actualizados. Debe incluir un ID válido.
     * @return {@link ResponseEntity} con el {@link CategoryResponseDTO} actualizado y código HTTP 200 OK.
     */
    @PutMapping
    public ResponseEntity<CategoryResponseDTO> updateCategory(@RequestBody Category category) {
        CategoryResponseDTO updatedCategory = catService.updateCategory(category);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    /**
     * Elimina una categoría por su ID.
     *
     * @param id El ID de la categoría a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        return new ResponseEntity<>(catService.deleteCategory(id), HttpStatus.OK);
    }

    // ** SUBCATEGORIES **

    /**
     * Crea una nueva subcategoría asociada a una categoría existente.
     *
     * @param subcategory La entidad {@link Subcategory} a crear. Debe incluir el ID de su categoría padre.
     * @return {@link ResponseEntity} con el {@link SubcategoryResponseDTO} creado y código HTTP 200 OK.
     */
    @PostMapping("/subcategories")
    public ResponseEntity<SubcategoryResponseDTO> createSubcategory(@RequestBody Subcategory subcategory) {
        SubcategoryResponseDTO savedSubcategory = subcatService.createSubcategory(subcategory);
        return new ResponseEntity<>(savedSubcategory, HttpStatus.OK);
    }

    /**
     * Actualiza una subcategoría existente con los nuevos datos proporcionados.
     *
     * @param subcategory La entidad {@link Subcategory} con los datos actualizados. Debe incluir un ID válido.
     * @return {@link ResponseEntity} con el {@link SubcategoryResponseDTO} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/subcategories")
    public ResponseEntity<SubcategoryResponseDTO> updateSubcategory(@RequestBody Subcategory subcategory) {
        SubcategoryResponseDTO updatedSubcategory = subcatService.updateSubcategory(subcategory);
        return new ResponseEntity<>(updatedSubcategory, HttpStatus.OK);
    }

    /**
     * Elimina una subcategoría por su ID.
     *
     * @param id El ID de la subcategoría a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP 200 OK.
     */
    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<String> deleteSubcategory(@PathVariable Long id) {
        return new ResponseEntity<>(subcatService.deleteSubcategory(id), HttpStatus.OK);
    }
}
