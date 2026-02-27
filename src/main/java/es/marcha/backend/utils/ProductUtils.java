package es.marcha.backend.utils;

import es.marcha.backend.exception.ProductException;
import es.marcha.backend.model.ecommerce.Product;

/**
 * Clase de utilidad para la generación automática de información
 * relacionada con productos como slugs, SKU y metadatos SEO.
 *
 * Todos los métodos son estáticos y están orientados a facilitar
 * la creación de información básica para e-commerce.
 */
public class ProductUtils {

    /**
     * Genera un slug amigable para URL a partir del nombre del producto.
     *
     * Convierte el texto a minúsculas y reemplaza cualquier carácter
     * que no sea alfanumérico por un guion.
     *
     * Ejemplo:
     * "Nike Air Pegasus 40!" -> "nike-air-pegasus-40-"
     *
     * Nota: No elimina tildes ni evita múltiples guiones consecutivos.
     *
     * @param name Nombre original del producto
     * @return Slug generado para uso en URLs
     */
    public static String createSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "-");
    }

    /**
     * Genera un SKU pseudo-único basado en el nombre del producto
     * y el timestamp actual del sistema.
     *
     * El SKU se compone de:
     * - Un prefijo con las tres primeras letras del nombre en mayúsculas
     * (o el nombre completo si tiene menos de tres caracteres).
     * - Una parte numérica derivada del tiempo actual en milisegundos.
     *
     * Ejemplo:
     * "Nike" -> "NIK123456"
     *
     * Nota: No garantiza unicidad absoluta en entornos de alta concurrencia.
     *
     * @param name Nombre del producto
     * @return SKU generado
     */
    public static String generateSKU(String name) {
        String prefix = name.length() >= 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
        String uniquePart = String.valueOf(System.currentTimeMillis()).substring(7);
        return prefix + uniquePart;
    }

    /**
     * Genera un meta title genérico optimizado para SEO en inglés.
     *
     * El nombre del producto se coloca al inicio seguido de
     * un texto comercial orientado a mejorar el CTR.
     *
     * @param name Nombre del producto
     * @return Meta title en inglés
     */

    public static String generateTitleEN(String name) {
        return name + " | Premium Quality & Fast Shipping";
    }

    /**
     * Genera un meta title genérico optimizado para SEO en español.
     *
     * El nombre del producto se coloca al inicio seguido de
     * un texto comercial orientado a mejorar el CTR.
     *
     * @param name Nombre del producto
     * @return Meta title en español
     */
    public static String generateTitleES(String name) {
        return name + " | Calidad Premium y Envío Rápido";
    }

    /**
     * Genera una meta description genérica orientada a SEO en inglés.
     *
     * Incluye el nombre del producto dentro de un texto persuasivo
     * pensado para mejorar el porcentaje de clics (CTR).
     *
     * @param name Nombre del producto
     * @return Meta description en inglés
     */
    public static String generateMetaDescriptionEN(String name) {
        return "Discover " + name
                + ", crafted for quality and performance. Enjoy fast shipping, secure checkout, and exceptional value in every purchase.";
    }

    /**
     * Genera una meta description genérica orientada a SEO en español.
     *
     * Incluye el nombre del producto dentro de un texto persuasivo
     * pensado para mejorar el porcentaje de clics (CTR).
     *
     * @param name Nombre del producto
     * @return Meta description en español
     */
    public static String generateMetaDescriptionES(String name) {
        return "Descubre " + name
                + ", diseñado con calidad y rendimiento. Disfruta de envío rápido, pago seguro y un valor excepcional en cada compra.";
    }

    public static void updateRating(Product product, double newPuntuation) {
        if (product.getRating() == null) {
            product.setRating(0.0);
        }
        if (product.getRatingCount() == null) {
            product.setRatingCount(0.0);
        }

        double currentRating = product.getRating();
        double currentRatingCount = product.getRatingCount();

        double newRating = ((currentRating * currentRatingCount) + newPuntuation) / (currentRatingCount + 1);

        product.setRating(newRating);
        product.setRatingCount(currentRatingCount + 1);
    }

    public static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ProductException(ProductException.NOT_VALID_RATING);
        }
    }

}
