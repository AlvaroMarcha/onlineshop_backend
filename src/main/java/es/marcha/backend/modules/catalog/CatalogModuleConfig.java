package es.marcha.backend.modules.catalog;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración del módulo Catalog.
 *
 * <p>
 * Permite activar o desactivar completamente este módulo mediante la propiedad:
 * 
 * <pre>
 * modules.catalog.enabled = true | false
 * </pre>
 *
 * <p>
 * Por defecto el módulo está habilitado (matchIfMissing = true).
 * Cuando se deshabilita, los endpoints REST del módulo dejan de registrarse
 * en el contexto de Spring.
 */
@Configuration

public class CatalogModuleConfig {
}
