package es.marcha.backend.modules.catalog.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.catalog.application.dto.request.ProductRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.request.ProductSearchFilter;
import es.marcha.backend.modules.catalog.application.dto.response.product.ProductResponseDTO;
import es.marcha.backend.modules.catalog.application.dto.response.product.ProductReviewResponseDTO;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.application.mapper.ProductMapper;
import es.marcha.backend.modules.catalog.domain.model.Category;
import es.marcha.backend.modules.catalog.domain.model.Inventory;
import es.marcha.backend.modules.catalog.domain.model.Subcategory;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.infrastructure.persistence.InventoryRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.specification.ProductSpecification;
import es.marcha.backend.core.shared.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository prodRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductReviewService prService;

    @Autowired
    private SubcategoryService subcategoryService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Inyectado con @Lazy para evitar dependencia circular con ProductImageService
     */
    @Autowired
    @Lazy
    private ProductImageService imageService;

    public static final String PRODUCT_DELETED = "PRODUCT WAS DELETED";

    /**
     * Obtiene un producto activo y no eliminado por su ID.
     *
     * @param id El ID del producto a buscar.
     * @return {@link ProductResponseDTO} con los datos del producto.
     * @throws ProductException si el producto no existe, está inactivo o eliminado.
     */
    public ProductResponseDTO getProductById(long id) {
        return prodRepository.findById(id)
                .filter(p -> p.isActive() && !p.isDeleted())
                .map(ProductMapper::toProductDetailDTO)
                .orElseThrow(() -> new ProductException());
    }

    /**
     * Obtiene todos los productos activos y no eliminados del sistema,
     * incluyendo las reseñas activas de cada uno.
     *
     * @return Lista de {@link ProductResponseDTO} con todos los productos
     *         disponibles.
     * @throws ProductException si no hay productos activos.
     */
    public List<ProductResponseDTO> getAllProducts() {
        List<ProductResponseDTO> products = prodRepository.findAll().stream()
                .filter(p -> p.isActive() && !p.isDeleted())
                .map(ProductMapper::toProductDTO)
                .toList();

        // Conseguir lista de reviews filtradas
        products.forEach(product -> {
            List<ProductReviewResponseDTO> reviews = prService.getAllReviewsByProductHandler(product.getId());
            product.setReviews(reviews);
        });

        if (products == null || products.isEmpty()) {
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
    @Transactional
    @SuppressWarnings("deprecation") // Autorizados a sincronizar Product.categories desde subcategorías
    public ProductResponseDTO createProduct(Product product) {
        if (product == null) {
            throw new ProductException(ProductException.FAILED_CREATE);
        }

        String name = product.getName().trim();

        // Auditoría
        if (product.getCreatedBy() == null || product.getCreatedBy().isBlank()) {
            product.setCreatedBy(LocalDateTime.now().toString());
        }
        // Precio con descuento: si no se pasa, igual al precio base
        if (product.getDiscountPrice() == null) {
            product.setDiscountPrice(product.getPrice());
        }
        // IVA: si no se pasa, 21 % por defecto
        if (product.getTaxRate() == null) {
            product.setTaxRate(new BigDecimal("0.21"));
        }
        // Reviews inicializadas a lista vacía para evitar NPE
        product.setReviews(new ArrayList<>());

        product.setSku(ProductUtils.generateSKU(name));
        product.setActive(true);
        product.setDeleted(false);
        product.setVersion(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setSlug(ProductUtils.createSlug(name));
        product.setMetaTitle(ProductUtils.generateTitleES(name));
        product.setMetaDescription(ProductUtils.generateMetaDescriptionES(name));
        product.setRating(0.0);
        product.setRatingCount(0.0);
        // Validar stock inicial — debe ser >= 1
        if (product.getStock() < 1) {
            throw new ProductException(ProductException.INVALID_INITIAL_STOCK);
        }
        if (product.getLowStockThreshold() == null) {
            product.setLowStockThreshold(5);
        }

        // Validar que tenga categorías O subcategorías (al menos una)
        boolean hasSubcategories = product.getSubcategories() != null && !product.getSubcategories().isEmpty();
        boolean hasCategories = product.getCategories() != null && !product.getCategories().isEmpty();

        if (!hasSubcategories && !hasCategories) {
            throw new IllegalArgumentException("El producto debe tener al menos una categoría o subcategoría");
        }

        // Caso 1: Si tiene subcategorías → Derivar categorías desde subcategorías
        // (Opción 1)
        if (hasSubcategories) {
            List<Category> categories = product.getSubcategories().stream()
                    .map(Subcategory::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
            product.setCategories(categories);
        }
        // Caso 2: Si solo tiene categorías → Usar categorías directas, subcategorías
        // vacías
        // (product.getCategories() ya está asignado desde el mapper)

        Product saved = prodRepository.save(product);

        // Crear registro de Inventario con el stock inicial del producto
        Inventory inventory = Inventory.builder()
                .product(saved)
                .quantity(saved.getStock())
                .reservedQuantity(0)
                .minStock(5)
                .maxStock(0)
                .incomingStock(0)
                .damagedStock(0)
                .updatedAt(LocalDateTime.now())
                .build();
        inventoryRepository.save(inventory);

        return ProductMapper.toProductDTO(saved);
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
     * <p>
     * Las subcategorías se actualizan desde {@code subcategoryIds} del DTO.
     * Las categorías se derivan automáticamente de las subcategorías asignadas
     * para mantener consistencia (Opción 1: derivar categorías de subcategorías).
     * </p>
     *
     * @param id  ID del producto a actualizar
     * @param dto DTO con los datos actualizados del producto
     * @return el producto actualizado mapeado a {@link ProductResponseDTO}
     * @throws ProductException si el producto no existe o la actualización falla
     */
    @Transactional
    @SuppressWarnings("deprecation") // Autorizados a sincronizar Product.categories desde subcategorías
    public ProductResponseDTO updateProduct(long id, ProductRequestDTO dto) {
        Product product = prodRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_UPDATE));

        // El SKU es inmutable una vez generado — no se sobreescribe
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setTaxRate(dto.getTaxRate());
        product.setUpdatedAt(LocalDateTime.now());
        product.setWeight(dto.getWeight());
        product.setDigital(dto.isDigital());
        product.setFeatured(dto.isFeatured());

        // Actualizar categorías/subcategorías si se proporcionan
        boolean hasSubcategoryIds = dto.getSubcategoryIds() != null && !dto.getSubcategoryIds().isEmpty();
        boolean hasCategoryIds = dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty();

        if (hasSubcategoryIds) {
            // Caso 1: Tiene subcategorías → Derivar categorías desde subcategorías
            List<Subcategory> subcategories = subcategoryService.getAllSubcategoriesHandler(dto.getSubcategoryIds());
            product.setSubcategories(subcategories);

            List<Category> categories = subcategories.stream()
                    .map(Subcategory::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
            product.setCategories(categories);

        } else if (hasCategoryIds) {
            // Caso 2: Solo tiene categorías → Usar categorías directas, limpiar
            // subcategorías
            List<Category> categories = categoryService.getAllCategoriesHandler(dto.getCategoryIds());
            product.setCategories(categories);
            product.setSubcategories(java.util.Collections.emptyList());
        }
        // Si no se envían ni categoryIds ni subcategoryIds → No actualizar (mantener
        // actuales)

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

        // Eliminar ficheros de imagen del disco antes del soft-delete
        imageService.deleteAllFilesForProduct(id);

        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        prodRepository.save(product);
        return PRODUCT_DELETED;
    }

    /**
     * Actualiza el stock de un producto manualmente (uso admin).
     * <p>
     * Permite al administrador corregir el inventario desde el dashboard
     * sin necesidad de crear o cancelar pedidos.
     * </p>
     *
     * @param id       ID del producto cuyo stock se actualiza
     * @param newStock nuevo valor de stock (debe ser &gt;= 0)
     * @return mensaje de confirmación de actualización
     * @throws ProductException si el producto no existe o el stock es negativo
     */
    @Transactional
    public String updateProductStock(long id, int newStock) {
        if (newStock < 0)
            throw new ProductException(ProductException.FAILED_UPDATE);

        Product product = prodRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        prodRepository.save(product);

        return ProductException.STOCK_UPDATED;
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

    /**
     * Busca productos aplicando filtros opcionales y paginación.
     * <p>
     * Todos los filtros son combinables entre sí. Los usuarios sin rol de
     * administrador solo ven productos con {@code isActive = true}.
     * Los admins pueden pasar {@code includeInactive = true} para ver también
     * los inactivos.
     * </p>
     * <p>
     * Ordenación:
     * <ul>
     * <li>newest=true → por createdAt DESC (más recientes primero)</li>
     * <li>newest=false (defecto) → por soldCount DESC (más vendidos primero)</li>
     * </ul>
     * </p>
     *
     * @param filter  parámetros de búsqueda y paginación
     * @param isAdmin {@code true} si el usuario autenticado tiene rol ADMIN o
     *                SUPER_ADMIN
     * @return página de {@link ProductResponseDTO} con metadatos de paginación
     */
    public Page<ProductResponseDTO> searchProducts(ProductSearchFilter filter, boolean isAdmin) {
        // Validar tamaño de página para evitar sobrecargas
        int pageSize = Math.min(Math.max(filter.getSize(), 1), 100);
        int pageNumber = Math.max(filter.getPage(), 0);

        // Ordenación: newest=true → createdAt DESC, por defecto → soldCount DESC
        Sort sort = (filter.getNewest() != null && filter.getNewest())
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.DESC, "soldCount");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Construir Specification con los filtros opcionales
        Specification<Product> spec = ProductSpecification.build(filter, isAdmin);

        return prodRepository.findAll(spec, pageable)
                .map(ProductMapper::toProductDTO);
    }
}
