package es.marcha.backend.modules.cart;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración del módulo Cart.
 *
 * <p>
 * Permite activar o desactivar completamente este módulo mediante la propiedad:
 * 
 * <pre>
 * modules.cart.enabled = true | false
 * </pre>
 *
 * <p>
 * Por defecto el módulo está habilitado (matchIfMissing = false).
 * Cuando se deshabilita, los endpoints REST del módulo dejan de registrarse
 * en el contexto de Spring.
 */
@Configuration

public class CartModuleConfig {
}
