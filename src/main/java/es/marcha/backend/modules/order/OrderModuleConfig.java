package es.marcha.backend.modules.order;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración del módulo Order.
 *
 * <p>
 * Permite activar o desactivar completamente este módulo mediante la propiedad:
 * 
 * <pre>
 * modules.order.enabled = true | false
 * </pre>
 *
 * <p>
 * Por defecto el módulo está deshabilitado (matchIfMissing = false).
 * Cuando se deshabilita, los endpoints REST del módulo dejan de registrarse
 * en el contexto de Spring (incluye pedidos y pagos con Stripe).
 */
@Configuration

public class OrderModuleConfig {
}
