package es.marcha.backend.modules.cart.domain.enums;

public enum CartStatus {
    /** Carrito activo: el usuario lo est├í usando */
    ACTIVE,
    /** Expirado por inactividad tras 2 horas sin modificaciones */
    EXPIRED,
    /** Convertido en pedido confirmado */
    CONVERTED
}