package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.ecommerce.product.ProductVariantRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.variant.ProductVariantResponseDTO;
import es.marcha.backend.core.error.exception.ProductAttribException;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.mapper.ecommerce.ProductVariantMapper;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.ecommerce.product.ProductAttrib;
import es.marcha.backend.model.ecommerce.product.ProductAttribValue;
import es.marcha.backend.model.ecommerce.product.ProductVariant;
import es.marcha.backend.model.ecommerce.product.ProductVariantAttrib;
import es.marcha.backend.repository.ecommerce.ProductAttribValueRepository;
import es.marcha.backend.repository.ecommerce.ProductRepository;
import es.marcha.backend.repository.ecommerce.ProductVariantAttribRepository;
import es.marcha.backend.repository.ecommerce.ProductVariantRepository;
import es.marcha.backend.core.shared.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class ProductVariantService {

    public static final String VARIANT_DELETED = "VARIANT WAS DELETED";

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductVariantAttribRepository variantAttribRepository;

    @Autowired
    private ProductAttribValueRepository attribValueRepository;

    @Autowired
    private ProductRepository productRepository;

    // ─── ProductVariant
    // ───────────────────────────────────────────────────────────

    /**
     * Obtiene una variante de producto por su ID.
     *
     * @param id identificador de la variante
     * @return DTO con los datos de la variante y sus atributos asignados
     * @throws ProductAttribException si la variante no existe
     */
    public ProductVariantResponseDTO getVariantById(long id) {
        return variantRepository.findById(id)
                .map(ProductVariantMapper::toResponseDTO)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.VARIANT_NOT_FOUND));
    }

    /**
     * Obtiene todas las variantes de un producto.
     *
     * @param productId identificador del producto
     * @return lista de DTOs con las variantes del producto
     * @throws ProductException       si el producto no existe
     * @throws ProductAttribException si el producto no tiene variantes
     */
    public List<ProductVariantResponseDTO> getVariantsByProduct(long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductException(ProductException.DEFAULT);
        }

        List<ProductVariantResponseDTO> variants = variantRepository.findAllByProductId(productId).stream()
                .map(ProductVariantMapper::toResponseDTO)
                .toList();

        if (variants.isEmpty()) {
            throw new ProductAttribException(ProductAttribException.FAILED_FETCH_VARIANTS);
        }

        return variants;
    }

    /**
     * Crea una nueva variante para un producto, asignando los valores de atributos
     * indicados.
     *
     * <p>
     * Validaciones aplicadas:
     * </p>
     * <ul>
     * <li>El SKU debe ser único en el sistema.</li>
     * <li>Cada attribValueId debe pertenecer a un atributo asignado al
     * producto.</li>
     * <li>No puede haber dos valores del mismo tipo de atributo en la misma
     * variante.</li>
     * <li>Si se marca como predeterminada, se desactiva la variante predeterminada
     * anterior.</li>
     * </ul>
     *
     * @param productId identificador del producto al que pertenece la variante
     * @param dto       datos de la variante a crear, incluyendo la lista de
     *                  attribValueIds
     * @return DTO con los datos de la variante creada y sus atributos
     * @throws ProductException       si el producto no existe
     * @throws ProductAttribException si el SKU ya existe, algún valor no pertenece
     *                                al producto
     *                                o hay atributos duplicados en la lista
     */
    @Transactional
    public ProductVariantResponseDTO createVariant(long productId, ProductVariantRequestDTO dto) {
        if (dto == null) {
            throw new ProductAttribException(ProductAttribException.FAILED_CREATE_VARIANT);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

        if (dto.isDefault()) {
            variantRepository.findByProductIdAndIsDefaultTrue(productId)
                    .ifPresent(current -> {
                        current.setDefault(false);
                        variantRepository.save(current);
                    });
        }

        ProductVariant variant = ProductVariantMapper.fromRequestDTO(dto, product);
        variant.setSku(ProductUtils.generateSKU(product.getName()));
        variant.setCreatedAt(LocalDateTime.now());
        ProductVariant saved = variantRepository.save(variant);

        if (dto.getAttribValueIds() != null) {
            for (Long attribValueId : dto.getAttribValueIds()) {
                ProductAttribValue attribValue = attribValueRepository.findById(attribValueId)
                        .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_VALUE_NOT_FOUND));

                ProductAttrib attrib = attribValue.getAttrib();
                long attribId = attrib.getId();

                boolean attribLinked = product.getAttribs() != null &&
                        product.getAttribs().stream().anyMatch(a -> a.getId() == attribId);
                if (!attribLinked) {
                    product.getAttribs().add(attrib);
                    productRepository.save(product);
                }

                if (variantAttribRepository.existsByVariantIdAndAttribValueAttribId(saved.getId(), attribId)) {
                    throw new ProductAttribException(ProductAttribException.DUPLICATE_ATTRIB_IN_VARIANT);
                }

                ProductVariantAttrib variantAttrib = ProductVariantAttrib.builder()
                        .variant(saved)
                        .attribValue(attribValue)
                        .build();
                variantAttribRepository.save(variantAttrib);
            }
        }

        return ProductVariantMapper.toResponseDTO(variantRepository.findById(saved.getId()).get());
    }

    /**
     * Actualiza los campos escalares de una variante existente (precios,
     * stock, estado). El SKU no se modifica; fue asignado automáticamente al crear.
     * No modifica los atributos asignados; para ello usar
     * {@link #addAttribValueToVariant}
     * o {@link #removeAttribValueFromVariant}.
     *
     * @param id  identificador de la variante a actualizar
     * @param dto datos actualizados de la variante
     * @return DTO con los datos de la variante actualizada
     * @throws ProductAttribException si la variante no existe o el nuevo SKU ya
     *                                está en uso
     */
    @Transactional
    public ProductVariantResponseDTO updateVariant(long id, ProductVariantRequestDTO dto) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.VARIANT_NOT_FOUND));

        if (dto.isDefault() && !variant.isDefault()) {
            variantRepository.findByProductIdAndIsDefaultTrue(variant.getProduct().getId())
                    .ifPresent(current -> {
                        current.setDefault(false);
                        variantRepository.save(current);
                    });
        }

        variant.setPriceOverride(dto.getPriceOverride());
        variant.setDiscountPriceOverride(dto.getDiscountPriceOverride());
        variant.setStock(dto.getStock());
        variant.setDefault(dto.isDefault());
        variant.setActive(dto.isActive());
        variant.setUpdatedAt(LocalDateTime.now());

        return ProductVariantMapper.toResponseDTO(variantRepository.save(variant));
    }

    /**
     * Elimina una variante y todos sus registros de atributos asignados en cascada.
     *
     * @param id identificador de la variante a eliminar
     * @return mensaje de confirmación de eliminación
     * @throws ProductAttribException si la variante no existe
     */
    @Transactional
    public String deleteVariant(long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.VARIANT_NOT_FOUND));

        Product product = variant.getProduct();
        product.getVariants().remove(variant);
        productRepository.save(product);
        return VARIANT_DELETED;
    }

    // ─── ProductVariantAttrib
    // ─────────────────────────────────────────────────────

    /**
     * Añade un valor de atributo a una variante existente.
     * Valida que el atributo pertenezca al producto y que no exista ya un valor
     * del mismo tipo de atributo asignado a la variante.
     *
     * @param variantId     identificador de la variante
     * @param attribValueId identificador del valor de atributo a añadir
     * @return DTO actualizado de la variante con el nuevo atributo incluido
     * @throws ProductAttribException si la variante o el valor no existen,
     *                                si el atributo no pertenece al producto
     *                                o si ya hay un valor del mismo atributo en la
     *                                variante
     */
    @Transactional
    public ProductVariantResponseDTO addAttribValueToVariant(long variantId, long attribValueId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.VARIANT_NOT_FOUND));

        ProductAttribValue attribValue = attribValueRepository.findById(attribValueId)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_VALUE_NOT_FOUND));

        long attribId = attribValue.getAttrib().getId();

        boolean attribBelongsToProduct = variant.getProduct().getAttribs().stream()
                .anyMatch(a -> a.getId() == attribId);

        if (!attribBelongsToProduct) {
            throw new ProductAttribException(ProductAttribException.ATTRIB_VALUE_NOT_BELONGS_TO_PRODUCT);
        }

        if (variantAttribRepository.existsByVariantIdAndAttribValueAttribId(variantId, attribId)) {
            throw new ProductAttribException(ProductAttribException.DUPLICATE_ATTRIB_IN_VARIANT);
        }

        ProductVariantAttrib variantAttrib = ProductVariantAttrib.builder()
                .variant(variant)
                .attribValue(attribValue)
                .build();
        variantAttribRepository.save(variantAttrib);

        return ProductVariantMapper.toResponseDTO(variantRepository.findById(variantId).get());
    }

    /**
     * Elimina un valor de atributo de una variante.
     *
     * @param variantId       identificador de la variante
     * @param variantAttribId identificador del registro ProductVariantAttrib a
     *                        eliminar
     * @return DTO actualizado de la variante sin el atributo eliminado
     * @throws ProductAttribException si la variante o el registro de atributo no
     *                                existen
     */
    @Transactional
    public ProductVariantResponseDTO removeAttribValueFromVariant(long variantId, long variantAttribId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ProductAttribException(ProductAttribException.VARIANT_NOT_FOUND);
        }

        ProductVariantAttrib variantAttrib = variantAttribRepository.findById(variantAttribId)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.FAILED_DELETE_VARIANT));

        variantAttribRepository.delete(variantAttrib);

        return ProductVariantMapper.toResponseDTO(variantRepository.findById(variantId).get());
    }
}
