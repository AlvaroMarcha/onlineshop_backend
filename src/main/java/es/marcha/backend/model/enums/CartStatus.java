package es.marcha.backend.model.enums;

public enum CartStatus {
    /** Carrito activo: el usuario lo está usando */
    ACTIVE,
    /** Expirado por inactividad tras 2 horas sin modificaciones */
    EXPIRED,
    /** Convertido en pedido confirmado */
    CONVERTED
}
