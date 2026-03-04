package es.marcha.backend.core.shared.domain.enums;

public enum MovementType {
    IN, // Entrada genérica (puede ser compra o ajuste positivo)
    OUT, // Salida genérica (venta o ajuste negativo)
    SALE, // Venta a cliente
    PURCHASE, // Compra a proveedor
    RETURN, // Devolución de cliente
    RESTOCK, // Reposición en almacén
    ADJUSTMENT // Ajuste manual por errores o inventario físico
}
