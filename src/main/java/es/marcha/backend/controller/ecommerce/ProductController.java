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

import es.marcha.backend.dto.request.ecommerce.ProductRequestDTO;
import es.marcha.backend.dto.response.ecommerce.ProductResponseDTO;
import es.marcha.backend.dto.response.ecommerce.ProductReviewResponseDTO;
import es.marcha.backend.mapper.ProductMapper;
import es.marcha.backend.model.ecommerce.Product;
import es.marcha.backend.model.ecommerce.ProductReview;
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.services.ecommerce.ProductReviewService;
import es.marcha.backend.services.ecommerce.ProductService;
import es.marcha.backend.services.ecommerce.SubcategoryService;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService prodService;

    @Autowired
    private ProductReviewService rService;

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
        List<Subcategory> subcategories = subcatService.getAllSubcategoriesHandler(productDTO.getSubcategoryIds());
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

    /**
     * REVIEWS
     */

    @GetMapping("/reviews/{id}")
    public ResponseEntity<List<ProductReviewResponseDTO>> getProductReviews(@PathVariable long id) {
        List<ProductReviewResponseDTO> reviews = rService.getAllReviewsByProduct(id);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @PostMapping("/reviews")
    public ResponseEntity<ProductReviewResponseDTO> createProductReview(@RequestBody ProductReview review) {
        ProductReviewResponseDTO createdReview = rService.addNewReview(review);
        return new ResponseEntity<>(createdReview, HttpStatus.OK);
    }

    @PutMapping("/reviews")
    public ResponseEntity<ProductReviewResponseDTO> updateProductReview(@RequestBody ProductReview review) {
        ProductReviewResponseDTO updatedReview = rService.updateReview(review);
        return new ResponseEntity<>(updatedReview, HttpStatus.OK);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteProductReview(@PathVariable long id) {
        return new ResponseEntity<>(rService.deleteReview(id), HttpStatus.OK);
    }

}
