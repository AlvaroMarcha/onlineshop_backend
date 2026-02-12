package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.ProductResponseDTO;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.mapper.ProductMapper;
import es.marcha.backend.model.ecommerce.Product;
import es.marcha.backend.repository.ecommerce.ProductRepository;
import es.marcha.backend.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository prodRepository;

    public static final String PRODUCT_DELETED = "PRODUCT WAS DELETED";

    public ProductResponseDTO getProductById(long id) {
        return prodRepository.findById(id)
                .filter(p -> p.isActive() && !p.isDeleted())
                .map(ProductMapper::toProductDTO)
                .orElseThrow(() -> new ProductException());
    }

    public List<ProductResponseDTO> getAllProducts() {
        List<ProductResponseDTO> products = prodRepository.findAll().stream()
                .filter(p -> p.isActive() && !p.isDeleted())
                .map(ProductMapper::toProductDTO)
                .toList();
        if (products.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH);
        }
        return products;
    }

    /**
     * Crea un nuevo producto en el sistema.
     *
     * <p>
     * Este método valida que el objeto {@link Product} no sea nulo,
     * inicializa los valores por defecto relacionados con el estado y auditoría,
     * persiste la entidad en la base de datos y retorna el resultado mapeado
     * a {@link ProductResponseDTO}.
     * </p>
     *
     * <ul>
     * <li>Establece {@code active} en {@code true}</li>
     * <li>Establece {@code deleted} en {@code false}</li>
     * <li>Inicializa {@code version} en {@code 0}</li>
     * <li>Asigna la fecha y hora actual a {@code createdAt} y
     * {@code updatedAt}</li>
     * </ul>
     *
     * @param product objeto {@link Product} que se desea crear
     * @return el producto creado mapeado a {@link ProductResponseDTO}
     * @throws ProductException si el producto es {@code null} o la creación falla
     */
    public ProductResponseDTO createProduct(Product product) {
        if (product == null) {
            throw new ProductException(ProductException.FAILED_CREATE);
        }

        String name = product.getName().trim();

        product.setActive(true);
        product.setDeleted(false);
        product.setVersion(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setSlug(ProductUtils.createSlug(name));
        product.setMetaTitle(ProductUtils.generateTitleES(name));
        product.setMetaDescription(ProductUtils.generateMetaDescriptionES(name));

        return ProductMapper.toProductDTO(prodRepository.save(product));
    }

    /**
     * Actualiza un producto existente en el sistema.
     *
     * <p>
     * Este método busca el producto persistido por su ID,
     * actualiza sus campos modificables con los valores proporcionados,
     * actualiza la marca de tiempo {@code updatedAt} y guarda los cambios
     * dentro de un contexto transaccional.
     * </p>
     *
     * @param updatedProduct objeto {@link Product} que contiene los datos
     *                       actualizados
     * @return el producto actualizado mapeado a {@link ProductResponseDTO}
     * @throws ProductException si el producto no existe o la actualización falla
     */
    @Transactional
    public ProductResponseDTO updateProduct(Product updatedProduct) {
        Product product = prodRepository.findById(updatedProduct.getId())
                .orElseThrow(() -> new ProductException(ProductException.FAILED_UPDATE));

        product.setName(updatedProduct.getName());
        product.setSku(updatedProduct.getSku());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setDiscountPrice(updatedProduct.getDiscountPrice());
        product.setTaxRate(updatedProduct.getTaxRate());
        product.setUpdatedAt(LocalDateTime.now());
        product.setWeight(updatedProduct.getWeight());
        product.setDigital(updatedProduct.isDigital());
        product.setFeatured(updatedProduct.isFeatured());

        return ProductMapper.toProductDTO(prodRepository.save(product));
    }

    /**
     * Realiza la eliminación lógica de un producto.
     *
     * <p>
     * Este método busca el producto por su identificador. Si existe,
     * marca el registro como eliminado estableciendo {@code deleted} en
     * {@code true}
     * y asigna la fecha y hora actual a {@code deletedAt}.
     * No elimina físicamente el registro de la base de datos.
     * </p>
     *
     * @param id identificador del producto a eliminar
     * @return mensaje de confirmación de eliminación
     * @throws ProductException si el producto no existe o la operación falla
     */
    public String deleteProduct(long id) {
        Product product = prodRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_DELETE));
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        prodRepository.save(product);
        return PRODUCT_DELETED;
    }

    /**
     * Calcula el promedio de calificación de un producto.
     *
     * <p>
     * Si el producto no tiene calificaciones registradas
     * ({@code ratingCount == 0}), el método retorna {@code 0}
     * para evitar divisiones por cero.
     * </p>
     *
     * @param product producto del cual se calculará el promedio de calificación
     * @return promedio de calificación del producto
     */
    public double calculateProductRating(Product product) {
        if (product.getRatingCount() == 0) {
            return 0;
        }
        return product.getRating() / product.getRatingCount();
    }

    /**
     * Suma una nueva calificación al producto y actualiza el contador.
     *
     * <p>
     * Este método obtiene el producto por su ID, incrementa la suma
     * total de calificaciones ({@code rating}) con el nuevo valor recibido,
     * incrementa el contador ({@code ratingCount}) en uno y persiste los cambios.
     * </p>
     *
     * @param id        identificador del producto
     * @param newRating nueva calificación a agregar
     * @return suma total acumulada de calificaciones después de la actualización
     * @throws ProductException si el producto no existe o la operación falla
     */
    public double sumRating(long id, double newRating) {
        Product product = prodRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH));
        product.setRating(product.getRating() + newRating);
        product.setRatingCount(product.getRatingCount() + 1);
        prodRepository.save(product);
        return product.getRating();
    }
}
