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

import es.marcha.backend.dto.request.ProductRequestDTO;
import es.marcha.backend.dto.response.ProductResponseDTO;
import es.marcha.backend.mapper.ProductMapper;
import es.marcha.backend.model.ecommerce.Product;
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.services.ecommerce.ProductService;
import es.marcha.backend.services.ecommerce.SubcategoryService;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService prodService;

    @Autowired
    private SubcategoryService subcatService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = prodService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable long id) {
        ProductResponseDTO product = prodService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO productDTO) {
        List<Subcategory> subcategories = subcatService.getAllSubcategories(productDTO.getSubcategoryIds());
        Product product = ProductMapper.toProductByRequestProduct(productDTO, subcategories);
        ProductResponseDTO createdProduct = prodService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<ProductResponseDTO> updateProduct(@RequestBody Product product) {
        ProductResponseDTO updatedProduct = prodService.updateProduct(product);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        return new ResponseEntity<>(prodService.deleteProduct(id), HttpStatus.OK);
    }

}
