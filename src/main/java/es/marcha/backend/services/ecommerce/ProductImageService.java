package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.dto.request.ecommerce.ProductImageReorderItemDTO;
import es.marcha.backend.dto.request.ecommerce.ProductImageUpdateRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.ProductImageResponseDTO;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.exception.ProductImageException;
import es.marcha.backend.mapper.ecommerce.ProductMapper;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.ecommerce.product.ProductImage;
import es.marcha.backend.repository.ecommerce.ProductImageRepository;
import es.marcha.backend.repository.ecommerce.ProductRepository;
import es.marcha.backend.services.media.MediaService;

@Service
public class ProductImageService {

    /** Número máximo de imágenes por producto. */
    private static final int MAX_IMAGES_PER_PRODUCT = 5;

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final MediaService mediaService;

    public ProductImageService(ProductImageRepository imageRepository,
            ProductRepository productRepository,
            MediaService mediaService) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.mediaService = mediaService;
    }

    // =========================================================================
    // POST /products/{productId}/images
    // =========================================================================

    /**
     * Sube una o varias imágenes a la galería del producto.
     *
     * <p>
     * Reglas aplicadas:
     * </p>
     * <ul>
     * <li>Máximo de {@value MAX_IMAGES_PER_PRODUCT} imágenes por producto.</li>
     * <li>Si el producto no tiene ninguna imagen todavía, la primera imagen
     * subida se marca automáticamente como principal ({@code isMain = true}).</li>
     * <li>El {@code sortOrder} se asigna automáticamente a partir del mayor
     * valor existente + 1.</li>
     * </ul>
     *
     * @param productId ID del producto
     * @param files     ficheros de imagen recibidos en el form-data
     * @return lista de DTOs de las imágenes recién creadas
     */
    @Transactional
    public List<ProductImageResponseDTO> uploadImages(long productId, List<MultipartFile> files) {
        Product product = findProduct(productId);

        long currentCount = imageRepository.countByProductId(productId);
        if (currentCount + files.size() > MAX_IMAGES_PER_PRODUCT) {
            throw new ProductImageException(ProductImageException.MAX_IMAGES_EXCEEDED);
        }

        boolean noMainExists = imageRepository.findFirstByProductIdAndIsMainTrue(productId).isEmpty();
        int nextSortOrder = (int) currentCount + 1;

        List<ProductImageResponseDTO> result = new ArrayList<>();
        boolean firstUploaded = true;

        for (MultipartFile file : files) {
            String filename = mediaService.saveProductImage(file, productId);
            String url = mediaService.buildProductImageUrl(filename, productId);

            boolean setAsMain = noMainExists && firstUploaded;

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(url)
                    .filename(filename)
                    .altText(null)
                    .sortOrder(nextSortOrder++)
                    .isMain(setAsMain)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            imageRepository.save(image);
            result.add(ProductMapper.toProductImageDTO(image));
            firstUploaded = false;
        }

        return result;
    }

    // =========================================================================
    // GET /products/{productId}/images
    // =========================================================================

    /**
     * Devuelve la lista de imágenes del producto ordenadas por {@code sortOrder}.
     *
     * @param productId ID del producto
     * @return lista de DTOs de imagen
     */
    public List<ProductImageResponseDTO> getImagesForProduct(long productId) {
        findProduct(productId);
        return imageRepository.findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(ProductMapper::toProductImageDTO)
                .toList();
    }

    // =========================================================================
    // PUT /products/{productId}/images/{imageId}
    // =========================================================================

    /**
     * Actualiza los metadatos de una imagen: {@code altText}, {@code sortOrder}
     * y/o {@code isMain}.
     *
     * <p>
     * Cuando {@code isMain = true} se envía, se limpia el flag {@code isMain}
     * del resto de imágenes del producto antes de establecer el nuevo principal.
     * </p>
     *
     * @param productId ID del producto
     * @param imageId   ID de la imagen
     * @param dto       campos a actualizar (todos opcionales)
     * @return DTO actualizado de la imagen
     */
    @Transactional
    public ProductImageResponseDTO updateImage(long productId, long imageId,
            ProductImageUpdateRequestDTO dto) {

        ProductImage image = findImage(productId, imageId);

        if (dto.getAltText() != null) {
            image.setAltText(dto.getAltText());
        }
        if (dto.getSortOrder() != null) {
            image.setSortOrder(dto.getSortOrder());
        }
        if (dto.getIsMain() != null && dto.getIsMain()) {
            // Desactivar el flag en todas las imágenes del producto y marcar esta
            imageRepository.clearMainFlagByProductId(productId);
            image.setMain(true);
        }

        imageRepository.save(image);
        return ProductMapper.toProductImageDTO(image);
    }

    // =========================================================================
    // DELETE /products/{productId}/images/{imageId}
    // =========================================================================

    /**
     * Elimina una imagen de la galería: borra el fichero del disco y el registro
     * de base de datos.
     *
     * <p>
     * Si la imagen eliminada era la principal ({@code isMain = true}), se
     * promueve automáticamente como nueva principal la imagen de menor
     * {@code sortOrder} restante, si existe.
     * </p>
     *
     * @param productId ID del producto
     * @param imageId   ID de la imagen a eliminar
     */
    @Transactional
    public void deleteImage(long productId, long imageId) {
        ProductImage image = findImage(productId, imageId);
        boolean wasMain = image.isMain();

        // Eliminar fichero del disco
        mediaService.deleteProductImageFile(productId, image.getFilename());

        // Eliminar registro de BD
        imageRepository.delete(image);

        // Si era la imagen principal, promover la siguiente con menor sortOrder
        if (wasMain) {
            imageRepository
                    .findFirstByProductIdAndIdNotOrderBySortOrderAsc(productId, imageId)
                    .ifPresent(next -> {
                        next.setMain(true);
                        imageRepository.save(next);
                    });
        }
    }

    // =========================================================================
    // PUT /products/{productId}/images/reorder
    // =========================================================================

    /**
     * Reordena las imágenes de un producto aplicando la lista de
     * {@link ProductImageReorderItemDTO} recibida.
     *
     * <p>
     * Solo se actualizan las imágenes cuyos IDs están en la lista y
     * pertenecen al producto indicado. Los IDs no reconocidos se ignoran.
     * </p>
     *
     * @param productId ID del producto
     * @param items     lista de pares {@code {id, sortOrder}} a aplicar
     * @return lista actualizada de imágenes del producto ordenada por el
     *         nuevo {@code sortOrder}
     */
    @Transactional
    public List<ProductImageResponseDTO> reorderImages(long productId,
            List<ProductImageReorderItemDTO> items) {

        findProduct(productId);
        List<ProductImage> existing = imageRepository.findByProductIdOrderBySortOrderAsc(productId);

        for (ProductImageReorderItemDTO item : items) {
            existing.stream()
                    .filter(img -> img.getId().equals(item.getId()))
                    .findFirst()
                    .ifPresent(img -> {
                        img.setSortOrder(item.getSortOrder());
                        imageRepository.save(img);
                    });
        }

        return imageRepository.findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(ProductMapper::toProductImageDTO)
                .toList();
    }

    // =========================================================================
    // Limpieza al eliminar producto
    // =========================================================================

    /**
     * Elimina del disco todos los ficheros de imagen de un producto.
     * Llamado desde {@link ProductService#deleteProduct(long)} en la eliminación
     * lógica.
     *
     * @param productId ID del producto cuyas imágenes se borran del disco
     */
    public void deleteAllFilesForProduct(long productId) {
        mediaService.deleteAllProductImageFiles(productId);
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    private Product findProduct(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));
    }

    private ProductImage findImage(long productId, long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ProductImageException(ProductImageException.NOT_FOUND));
        if (image.getProduct().getId() != productId) {
            // La imagen existe pero no pertenece a este producto
            throw new ProductImageException(ProductImageException.MISMATCH);
        }
        return image;
    }
}
