package es.marcha.backend.services.inventory;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.marcha.backend.dto.models.ProductDTO;
import es.marcha.backend.model.inventory.Product;
import es.marcha.backend.repository.inventory.ProductRepository;

@Service
public class ProductService {
    //Attribs
    @Autowired
    private ProductRepository productRepository;

    //GetAllProducts
    public List<ProductDTO> getAllProducts(){
        return toDTOList(this.productRepository.findAll());
    }

    //GetProductById
    public Optional<ProductDTO> getProductById(Long id){
        return this.productRepository.findById(id).map(this::toDTO);
    }
    

    //Transforms Product to DTO
    public ProductDTO toDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getStock(),
            product.getPrice(),
            product.getVisible(),
            product.getCategory() != null ? product.getCategory().getName() : null,
            product.getSubcategory() != null ? product.getSubcategory().getName() : null
        );
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
    return products.stream()
                   .map(this::toDTO) 
                   .toList(); 
    }


}