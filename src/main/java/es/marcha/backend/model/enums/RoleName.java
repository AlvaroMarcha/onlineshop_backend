package es.marcha.backend.model.enums;

/**
 * Roles del sistema. Cada valor se almacena con el prefijo {@code ROLE_}
 * para que Spring Security reconozca automáticamente las autoridades
 * al usar {@code hasRole()} / {@code hasAnyRole()}.
 *
 * <ul>
 * <li>{@code ROLE_SUPER_ADMIN} — acceso total al sistema.</li>
 * <li>{@code ROLE_ADMIN} — administración general.</li>
 * <li>{@code ROLE_ORDERS} — gestión de pedidos.</li>
 * <li>{@code ROLE_CUSTOMERS_INVOICES} — gestión de clientes y facturas.</li>
 * <li>{@code ROLE_SUPPORT} — atención al cliente (solo lectura de
 * usuarios).</li>
 * <li>{@code ROLE_STORE} — gestión de productos, categorías y variantes.</li>
 * <li>{@code ROLE_USER} — cliente registrado.</li>
 * </ul>
 */
public enum RoleName {
    ROLE_SUPER_ADMIN,
    ROLE_ADMIN,
    ROLE_ORDERS,
    ROLE_CUSTOMERS_INVOICES,
    ROLE_SUPPORT,
    ROLE_STORE,
    ROLE_USER
}
