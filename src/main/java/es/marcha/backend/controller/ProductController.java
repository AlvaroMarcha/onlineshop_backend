package es.marcha.backend.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import es.marcha.backend.dto.models.ProductDTO;
import es.marcha.backend.dto.request.ProductRequest;
import es.marcha.backend.model.inventory.Product;
import es.marcha.backend.services.inventory.ProductService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("backoffice/")
public class ProductController {
    //Attirbs
    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts(){
        List<ProductDTO> allProducts = this.productService.getAllProducts();
        return new ResponseEntity<List<ProductDTO>>(allProducts, HttpStatus.OK);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id){
        Optional<ProductDTO> productOpt = this.productService.getProductById(id); 
        ProductDTO product = null;
        ResponseEntity<ProductDTO> response = new ResponseEntity<ProductDTO>( HttpStatus.NOT_FOUND);
        if(productOpt.isPresent()){
            product = productOpt.get();
            response = new ResponseEntity<ProductDTO>(product, HttpStatus.OK);
        }
        return response;
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }
    
}
