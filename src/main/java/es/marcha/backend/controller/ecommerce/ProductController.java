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
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.ecommerce.product.ProductReview;
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

    /**
     * Obtiene todos los productos activos y no eliminados del sistema,
     * incluyendo sus reseñas.
     *
     * @return {@link ResponseEntity} con la lista de {@link ProductResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = prodService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Obtiene un producto activo y no eliminado por su ID.
     *
     * @param id El ID del producto a obtener.
     * @return {@link ResponseEntity} con el {@link ProductResponseDTO} correspondiente y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable long id) {
        ProductResponseDTO product = prodService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    /**
     * Crea un nuevo producto a partir de un DTO de entrada.
     * <p>
     * Resuelve las subcategorías por sus IDs antes de construir la entidad
     * y delegar la creación al servicio.
     * </p>
     *
     * @param productDTO DTO con los datos del producto, incluyendo la lista de IDs de subcategorías.
     * @return {@link ResponseEntity} con el {@link ProductResponseDTO} creado y código HTTP 200 OK.
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO productDTO) {
        List<Subcategory> subcategories = subcatService.getAllSubcategoriesHandler(productDTO.getSubcategoryIds());
        Product product = ProductMapper.toProductByRequestProduct(productDTO, subcategories);
        ProductResponseDTO createdProduct = prodService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.OK);
    }

    /**
     * Actualiza un producto existente con los nuevos datos proporcionados.
     *
     * @param product La entidad {@link Product} con los datos actualizados. Debe incluir un ID válido.
     * @return {@link ResponseEntity} con el {@link ProductResponseDTO} actualizado y código HTTP 200 OK.
     */
    @PutMapping
    public ResponseEntity<ProductResponseDTO> updateProduct(@RequestBody Product product) {
        ProductResponseDTO updatedProduct = prodService.updateProduct(product);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    /**
     * Realiza la eliminación lógica de un producto por su ID.
     * El producto no se elimina físicamente de la base de datos.
     *
     * @param id El ID del producto a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        return new ResponseEntity<>(prodService.deleteProduct(id), HttpStatus.OK);
    }

    /**
     * REVIEWS
     */

    /**
     * Obtiene todas las reseñas activas de un producto por el ID del producto.
     *
     * @param id El ID del producto cuyas reseñas se desean obtener.
     * @return {@link ResponseEntity} con la lista de {@link ProductReviewResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping("/reviews/{id}")
    public ResponseEntity<List<ProductReviewResponseDTO>> getProductReviews(@PathVariable long id) {
        List<ProductReviewResponseDTO> reviews = rService.getAllReviewsByProduct(id);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    /**
     * Crea una nueva reseña para un producto.
     *
     * @param review La entidad {@link ProductReview} a crear. Debe incluir el ID del producto y el ID del usuario.
     * @return {@link ResponseEntity} con el {@link ProductReviewResponseDTO} creado y código HTTP 200 OK.
     */
    @PostMapping("/reviews")
    public ResponseEntity<ProductReviewResponseDTO> createProductReview(@RequestBody ProductReview review) {
        ProductReviewResponseDTO createdReview = rService.addNewReview(review);
        return new ResponseEntity<>(createdReview, HttpStatus.OK);
    }

    /**
     * Actualiza una reseña existente con los nuevos datos proporcionados.
     *
     * @param review La entidad {@link ProductReview} con los datos actualizados. Debe incluir un ID válido.
     * @return {@link ResponseEntity} con el {@link ProductReviewResponseDTO} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/reviews")
    public ResponseEntity<ProductReviewResponseDTO> updateProductReview(@RequestBody ProductReview review) {
        ProductReviewResponseDTO updatedReview = rService.updateReview(review);
        return new ResponseEntity<>(updatedReview, HttpStatus.OK);
    }

    /**
     * Realiza la eliminación lógica de una reseña por su ID.
     *
     * @param id El ID de la reseña a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP 200 OK.
     */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteProductReview(@PathVariable long id) {
        return new ResponseEntity<>(rService.deleteReview(id), HttpStatus.OK);
    }

}
