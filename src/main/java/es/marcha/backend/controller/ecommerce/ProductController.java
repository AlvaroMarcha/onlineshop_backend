package es.marcha.backend.controller.ecommerce;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.dto.request.ecommerce.ProductImageReorderItemDTO;
import es.marcha.backend.dto.request.ecommerce.ProductImageUpdateRequestDTO;
import es.marcha.backend.dto.request.ecommerce.ProductRequestDTO;
import es.marcha.backend.dto.request.ecommerce.ProductSearchFilter;
import es.marcha.backend.dto.request.ecommerce.StockUpdateRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductImageResponseDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductResponseDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductReviewResponseDTO;
import es.marcha.backend.mapper.ecommerce.ProductMapper;
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.ecommerce.product.ProductReview;
import es.marcha.backend.services.ecommerce.ProductImageService;
import es.marcha.backend.services.ecommerce.ProductReviewService;
import es.marcha.backend.services.ecommerce.ProductService;
import es.marcha.backend.services.ecommerce.SubcategoryService;
import es.marcha.backend.services.wishlist.WishlistService;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService prodService;

    @Autowired
    private ProductReviewService rService;

    @Autowired
    private SubcategoryService subcatService;

    @Autowired
    private ProductImageService imageService;

    @Autowired
    private WishlistService wishlistService;

    /**
     * Busca productos con filtros opcionales y paginación.
     * <p>
     * Todos los parámetros son opcionales y combinables:
     * </p>
     * <ul>
     * <li>{@code q} — texto libre (nombre, descripción, slug)</li>
     * <li>{@code categoryId} — ID de categoría</li>
     * <li>{@code minPrice} / {@code maxPrice} — rango de precio</li>
     * <li>{@code featured} — solo productos destacados</li>
     * <li>{@code newest} — ordena por más recientes; por defecto ordena por más
     * vendidos</li>
     * <li>{@code includeInactive} — incluye productos inactivos (solo
     * ADMIN/SUPER_ADMIN)</li>
     * <li>{@code page} / {@code size} — paginación (defecto: page=0, size=20, máx:
     * 100)</li>
     * </ul>
     *
     * @param filter parámetros de búsqueda enlazados desde query string
     * @return {@link ResponseEntity} con {@link Page} de {@link ProductResponseDTO}
     *         y código 200 OK
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @ModelAttribute ProductSearchFilter filter) {

        // Determinar si el usuario autenticado tiene rol de admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Page<ProductResponseDTO> result = prodService.searchProducts(filter, isAdmin);

        // Enriquecer con isInWishlist si el usuario está autenticado
        Set<Long> wishlistIds = getWishlistProductIds(auth);
        if (!wishlistIds.isEmpty()) {
            result.getContent().forEach(dto -> dto.setIsInWishlist(wishlistIds.contains(dto.getId())));
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Obtiene todos los productos activos y no eliminados del sistema,
     * incluyendo sus reseñas.
     *
     * @return {@link ResponseEntity} con la lista de {@link ProductResponseDTO} y
     *         código HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = prodService.getAllProducts();

        // Enriquecer con isInWishlist si el usuario está autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<Long> wishlistIds = getWishlistProductIds(auth);
        if (!wishlistIds.isEmpty()) {
            products.forEach(dto -> dto.setIsInWishlist(wishlistIds.contains(dto.getId())));
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Obtiene un producto activo y no eliminado por su ID.
     *
     * @param id El ID del producto a obtener.
     * @return {@link ResponseEntity} con el {@link ProductResponseDTO}
     *         correspondiente y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable long id) {
        ProductResponseDTO product = prodService.getProductById(id);

        // Enriquecer con isInWishlist si el usuario está autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<Long> wishlistIds = getWishlistProductIds(auth);
        if (!wishlistIds.isEmpty()) {
            product.setIsInWishlist(wishlistIds.contains(product.getId()));
        }

        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    /**
     * Crea un nuevo producto a partir de un DTO de entrada.
     * <p>
     * Resuelve las subcategorías por sus IDs antes de construir la entidad
     * y delegar la creación al servicio.
     * </p>
     *
     * @param productDTO DTO con los datos del producto, incluyendo la lista de IDs
     *                   de subcategorías.
     * @return {@link ResponseEntity} con el {@link ProductResponseDTO} creado y
     *         código HTTP 200 OK.
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productDTO) {
        List<Subcategory> subcategories = subcatService.getAllSubcategoriesHandler(productDTO.getSubcategoryIds());
        Product product = ProductMapper.toProductByRequestProduct(productDTO, subcategories);
        ProductResponseDTO createdProduct = prodService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * Actualiza un producto existente con los nuevos datos proporcionados.
     *
     * @param product La entidad {@link Product} con los datos actualizados. Debe
     *                incluir un ID válido.
     * @return {@link ResponseEntity} con el {@link ProductResponseDTO} actualizado
     *         y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable long id,
            @RequestBody ProductRequestDTO productDTO) {
        ProductResponseDTO updatedProduct = prodService.updateProduct(id, productDTO);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    /**
     * Realiza la eliminación lógica de un producto por su ID.
     * El producto no se elimina físicamente de la base de datos.
     *
     * @param id El ID del producto a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP
     *         200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        return new ResponseEntity<>(prodService.deleteProduct(id), HttpStatus.OK);
    }

    /**
     * Actualiza el stock de un producto manualmente (solo admin).
     * <p>
     * Permite corregir el inventario desde el dashboard sin crear pedidos.
     * Devuelve 200 OK con un mensaje de confirmación.
     * Devuelve 400 Bad Request si el stock es negativo.
     * Devuelve 404 Not Found si el producto no existe.
     * </p>
     *
     * @param id      ID del producto cuyo stock se desea actualizar.
     * @param request DTO con el nuevo valor de stock.
     * @return {@link ResponseEntity} con mensaje de confirmación y código HTTP 200
     *         OK.
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<String> updateStock(
            @PathVariable long id,
            @RequestBody StockUpdateRequestDTO request) {
        String result = prodService.updateProductStock(id, request.getStock());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * IMAGES (Galería de imágenes del producto)
     */

    /**
     * Sube una o varias imágenes a la galería de un producto.
     *
     * <p>
     * Máximo {@code 10} imágenes por producto. Formatos permitidos: JPEG y PNG.
     * Tamaño máximo por fichero: 5 MB.
     * </p>
     *
     * @param productId ID del producto
     * @param files     ficheros de imagen (campo {@code files} en el form-data)
     * @return lista de DTOs de las imágenes recién creadas
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageResponseDTO>> uploadImages(
            @PathVariable long productId,
            @RequestParam("files") List<MultipartFile> files) {
        List<ProductImageResponseDTO> result = imageService.uploadImages(productId, files);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Devuelve todas las imágenes de la galería de un producto,
     * ordenadas por {@code sortOrder} ascendente.
     *
     * @param productId ID del producto
     * @return lista de DTOs de imagen
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageResponseDTO>> getImages(
            @PathVariable long productId) {
        List<ProductImageResponseDTO> images = imageService.getImagesForProduct(productId);
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    /**
     * Reordena las imágenes de la galería del producto.
     *
     * <p>
     * Recibe una lista de pares {@code {id, sortOrder}} y aplica los nuevos
     * valores a las imágenes correspondientes del producto.
     * </p>
     *
     * <p>
     * <strong>Nota:</strong> este endpoint debe declararse antes de
     * {@code /{imageId}} para evitar ambigüedades de ruta en Spring MVC.
     * </p>
     *
     * @param productId ID del producto
     * @param items     lista de reordenación
     * @return lista actualizada de imágenes ordenada por el nuevo {@code sortOrder}
     */
    @PutMapping("/{productId}/images/reorder")
    public ResponseEntity<List<ProductImageResponseDTO>> reorderImages(
            @PathVariable long productId,
            @RequestBody List<ProductImageReorderItemDTO> items) {
        List<ProductImageResponseDTO> result = imageService.reorderImages(productId, items);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Actualiza los metadatos de una imagen: {@code altText}, {@code sortOrder}
     * y/o {@code isMain}.
     *
     * <p>
     * Cuando {@code isMain = true} se envía, la imagen anterior principal
     * pierde el flag automáticamente.
     * </p>
     *
     * @param productId ID del producto
     * @param imageId   ID de la imagen
     * @param dto       campos a actualizar (todos opcionales)
     * @return DTO actualizado de la imagen
     */
    @PutMapping("/{productId}/images/{imageId}")
    public ResponseEntity<ProductImageResponseDTO> updateImage(
            @PathVariable long productId,
            @PathVariable long imageId,
            @RequestBody ProductImageUpdateRequestDTO dto) {
        ProductImageResponseDTO result = imageService.updateImage(productId, imageId, dto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Elimina una imagen de la galería del producto.
     *
     * <p>
     * Borra el fichero del disco y el registro de base de datos.
     * Si la imagen era la principal, se promueve automáticamente la
     * siguiente imagen (menor {@code sortOrder}) como nueva principal.
     * </p>
     *
     * @param productId ID del producto
     * @param imageId   ID de la imagen a eliminar
     * @return 204 No Content
     */
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable long productId,
            @PathVariable long imageId) {
        imageService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * REVIEWS
     */

    /**
     * Obtiene todas las reseñas activas de un producto por el ID del producto.
     *
     * @param id El ID del producto cuyas reseñas se desean obtener.
     * @return {@link ResponseEntity} con la lista de
     *         {@link ProductReviewResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping("/reviews/{id}")
    public ResponseEntity<List<ProductReviewResponseDTO>> getProductReviews(@PathVariable long id) {
        List<ProductReviewResponseDTO> reviews = rService.getAllReviewsByProduct(id);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    /**
     * Crea una nueva reseña para un producto.
     *
     * @param review La entidad {@link ProductReview} a crear. Debe incluir el ID
     *               del producto y el ID del usuario.
     * @return {@link ResponseEntity} con el {@link ProductReviewResponseDTO} creado
     *         y código HTTP 200 OK.
     */
    @PostMapping("/reviews")
    public ResponseEntity<ProductReviewResponseDTO> createProductReview(@RequestBody ProductReview review) {
        ProductReviewResponseDTO createdReview = rService.addNewReview(review);
        return new ResponseEntity<>(createdReview, HttpStatus.OK);
    }

    /**
     * Actualiza una reseña existente con los nuevos datos proporcionados.
     *
     * @param review La entidad {@link ProductReview} con los datos actualizados.
     *               Debe incluir un ID válido.
     * @return {@link ResponseEntity} con el {@link ProductReviewResponseDTO}
     *         actualizado y código HTTP 200 OK.
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
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP
     *         200 OK.
     */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteProductReview(@PathVariable long id) {
        return new ResponseEntity<>(rService.deleteReview(id), HttpStatus.OK);
    }

    // ─── Utilidades internas
    // ──────────────────────────────────────────────────────

    /**
     * Devuelve los IDs de productos en la wishlist del usuario autenticado.
     * Si no hay usuario autenticado o no tiene wishlist, devuelve un conjunto
     * vacío.
     *
     * @param auth objeto de autenticación del contexto de seguridad
     * @return {@link Set} de IDs de productos en la wishlist
     */
    private Set<Long> getWishlistProductIds(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Collections.emptySet();
        }
        try {
            return wishlistService.getWishlistProductIds((String) auth.getPrincipal());
        } catch (Exception e) {
            // Si hay cualquier error al obtener la wishlist, simplemente no enriquecemos
            return Collections.emptySet();
        }
    }

}
