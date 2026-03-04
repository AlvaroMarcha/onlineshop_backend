package es.marcha.backend.modules.invoice;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración del módulo Invoice.
 *
 * <p>
 * Permite activar o desactivar completamente este módulo mediante la propiedad:
 * 
 * <pre>
 * modules.invoice.enabled = true | false
 * </pre>
 *
 * <p>
 * Por defecto el módulo está deshabilitado (matchIfMissing = false).
 * Cuando se deshabilita, los endpoints REST del módulo dejan de registrarse
 * en el contexto de Spring.
 */
@Configuration

public class InvoiceModuleConfig {
}
