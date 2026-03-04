package es.marcha.backend.modules.catalog.domain.enums;

public enum MovementType {
    IN, // Entrada gen├®rica (puede ser compra o ajuste positivo)
    OUT, // Salida gen├®rica (venta o ajuste negativo)
    SALE, // Venta a cliente
    PURCHASE, // Compra a proveedor
    RETURN, // Devoluci├│n de cliente
    RESTOCK, // Reposici├│n en almac├®n
    ADJUSTMENT // Ajuste manual por errores o inventario f├¡sico
}