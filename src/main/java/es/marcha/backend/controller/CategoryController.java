package es.marcha.backend.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import es.marcha.backend.model.inventory.Attribute;
import es.marcha.backend.model.inventory.Category;
import es.marcha.backend.repository.inventory.CategoryRepository;

@RestController
@RequestMapping("/backoffice")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<Attribute>> getCategoriesAttributes(@PathVariable Long id){
         Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

                System.out.println(id + " " + category.getName() + " " + category.getAttributes().size());
        return ResponseEntity.ok(category.getAttributes());
    }
    
}
