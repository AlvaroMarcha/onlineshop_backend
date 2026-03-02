package es.marcha.backend.dto.request.ecommerce;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parámetros de búsqueda para el endpoint GET /products/search.
 * Todos los campos son opcionales y combinables entre sí.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductSearchFilter {

    /**
     * Texto libre: filtra por nombre, descripción o slug (insensible a mayúsculas).
     * Ejemplo: ?q=camiseta
     */
    private String q;

    /**
     * ID de categoría para filtrar productos pertenecientes a esa categoría.
     * Ejemplo: ?categoryId=3
     */
    private Long categoryId;

    /** Precio mínimo (inclusivo). Ejemplo: ?minPrice=10.00 */
    private BigDecimal minPrice;

    /** Precio máximo (inclusivo). Ejemplo: ?maxPrice=100.00 */
    private BigDecimal maxPrice;

    /**
     * Si es {@code true}, filtra solo productos destacados (isFeatured = true).
     * Ejemplo: ?featured=true
     */
    private Boolean featured;

    /**
     * Si es {@code true}, ordena por fecha de creación descendente (más nuevos
     * primero).
     * Por defecto se ordena por soldCount descendente.
     * Ejemplo: ?newest=true
     */
    private Boolean newest;

    /**
     * Si es {@code true} y el usuario tiene rol ADMIN o SUPER_ADMIN, incluye
     * también productos con isActive = false en los resultados.
     * Ignorado para usuarios sin rol de administrador.
     * Ejemplo: ?includeInactive=true
     */
    private boolean includeInactive;

    /** Número de página (base 0). Por defecto: 0. Ejemplo: ?page=0 */
    private int page = 0;

    /**
     * Tamaño de página. Por defecto: 20. Máximo recomendado: 100. Ejemplo: ?size=20
     */
    private int size = 20;
}
