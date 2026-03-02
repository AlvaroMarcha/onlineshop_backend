package es.marcha.backend.repository.ecommerce.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import es.marcha.backend.dto.request.ecommerce.ProductSearchFilter;
import es.marcha.backend.model.ecommerce.Category;
import es.marcha.backend.model.ecommerce.product.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

/**
 * Constructor de {@link Specification} para búsquedas dinámicas de productos.
 * <p>
 * Cada método privado representa un predicado opcional. Cuando el parámetro
 * es {@code null} o irrelevante, devuelve {@code null} para que
 * {@link Specification#where} lo ignore automáticamente.
 * </p>
 * <p>
 * Los filtros son combinables entre sí. Se aplican todos a la vez mediante AND.
 * </p>
 */
public class ProductSpecification {

    private ProductSpecification() {
    }

    /**
     * Construye la {@link Specification} combinada a partir del filtro recibido.
     *
     * @param filter  parámetros de búsqueda del usuario
     * @param isAdmin si el usuario tiene rol ADMIN o SUPER_ADMIN
     * @return {@link Specification} lista para pasar al repositorio
     */
    @SuppressWarnings("removal")
    public static Specification<Product> build(ProductSearchFilter filter, boolean isAdmin) {
        return Specification
                .where(notDeleted())
                .and(activeFilter(filter.isIncludeInactive(), isAdmin))
                .and(textSearch(filter.getQ()))
                .and(byCategory(filter.getCategoryId()))
                .and(minPrice(filter.getMinPrice()))
                .and(maxPrice(filter.getMaxPrice()))
                .and(featured(filter.getFeatured()));
    }

    // ── Predicados privados ───────────────────────────────────────────────

    /** Excluye siempre los productos borrados lógicamente. */
    private static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    /**
     * Filtra por isActive = true, salvo que el usuario sea admin y haya solicitado
     * incluir inactivos con {@code includeInactive = true}.
     */
    private static Specification<Product> activeFilter(boolean includeInactive, boolean isAdmin) {
        if (isAdmin && includeInactive) {
            // Admin con includeInactive=true: sin restricción de isActive
            return null;
        }
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    /**
     * Búsqueda de texto libre sobre nombre, descripción y slug.
     * Insensible a mayúsculas. Se usa OR entre los tres campos.
     */
    private static Specification<Product> textSearch(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return (root, query, cb) -> {
            String pattern = "%" + q.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern));
        };
    }

    /**
     * Filtra por categoría mediante JOIN con la tabla product_category.
     * Activa distinct para evitar duplicados cuando un producto tiene varias
     * categorías.
     */
    private static Specification<Product> byCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> {
            // distinct para evitar filas duplicadas por el JOIN ManyToMany
            query.distinct(true);
            Join<Product, Category> join = root.join("categories", JoinType.INNER);
            return cb.equal(join.get("id"), categoryId);
        };
    }

    /** Filtra productos con precio >= minPrice. */
    private static Specification<Product> minPrice(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /** Filtra productos con precio <= maxPrice. */
    private static Specification<Product> maxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /** Si featured = true, filtra solo productos con isFeatured = true. */
    private static Specification<Product> featured(Boolean featured) {
        if (featured == null || !featured) {
            return null;
        }
        return (root, query, cb) -> cb.isTrue(root.get("isFeatured"));
    }
}
