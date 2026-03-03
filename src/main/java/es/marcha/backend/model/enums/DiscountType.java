package es.marcha.backend.model.enums;

/**
 * Tipo de descuento de un cupón:
 * <ul>
 * <li>{@link #PERCENTAGE} — descuento como porcentaje sobre la base imponible
 * (p. ej. 10 %).</li>
 * <li>{@link #FIXED} — descuento de importe fijo en euros (p. ej. 5,00 €).</li>
 * </ul>
 */
public enum DiscountType {
    PERCENTAGE,
    FIXED
}
