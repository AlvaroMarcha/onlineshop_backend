package es.marcha.backend.services.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.marcha.backend.dto.models.ProductDTO;
import es.marcha.backend.dto.request.ProductRequest;
import es.marcha.backend.model.inventory.AttribValue;
import es.marcha.backend.model.inventory.Category;
import es.marcha.backend.model.inventory.Product;
import es.marcha.backend.model.inventory.Subcategory;
import es.marcha.backend.repository.inventory.AttribValueRepository;
import es.marcha.backend.repository.inventory.CategoryRepository;
import es.marcha.backend.repository.inventory.ProductRepository;
import es.marcha.backend.repository.inventory.SubcategoryRepository;
import jakarta.transaction.Transactional;

@Service
public class ProductService {
    //Attribs
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private AttribValueRepository valueRepository;

    //GetAllProducts
    public List<ProductDTO> getAllProducts(){
        return toDTOList(this.productRepository.findAll());
    }

    //GetProductById
    public Optional<ProductDTO> getProductById(Long id){
        return this.productRepository.findById(id).map(this::toDTO);
    }
    
    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUrlImg(request.getUrlImg());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setVisible(true);
        product.setDetails(request.getDetails());
        product.setSpecifications(request.getSpecifications());
        if (request.getImages() != null) {
            product.setImages(new ArrayList<>(request.getImages()));
        } else {
            product.setImages(new ArrayList<>());
        }

    // --- Asignar categoría ---
    if (request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + request.getCategoryId()));
        product.setCategory(category); // aquí ya pasas el objeto real, no el Optional
    }

    // --- Asignar subcategoría ---
    if (request.getSubcategoryId() != null) {
        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Subcategoría no encontrada con ID: " + request.getSubcategoryId()));
        product.setSubcategory(subcategory);
    }

    // --- Asignar atributos (valores) ---
    if (request.getValues() != null && !request.getValues().isEmpty()) {
        List<AttribValue> values = valueRepository.findAllById(request.getValues());
        product.setValues(values);
    }

    return productRepository.save(product);
}



    //Transforms Product to DTO
    public ProductDTO toDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getUrlImg(),
            product.getStock(),
            product.getPrice(),
            product.getVisible(),
            product.getCategory() != null ? product.getCategory().getName() : null,
            product.getSubcategory() != null ? product.getSubcategory().getName() : null,
            product.getImages() != null ? new ArrayList<>(product.getImages()) : List.of(),
            product.getDetails(),
            product.getSpecifications()
        );
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
    return products.stream()
                   .map(this::toDTO) 
                   .toList(); 
    }


}