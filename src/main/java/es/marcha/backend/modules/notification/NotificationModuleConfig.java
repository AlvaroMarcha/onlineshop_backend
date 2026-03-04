package es.marcha.backend.modules.notification;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración del módulo Notification.
 *
 * <p>
 * Permite activar o desactivar completamente este módulo mediante la propiedad:
 * 
 * <pre>
 * modules.notification.enabled = true | false
 * </pre>
 *
 * <p>
 * Por defecto el módulo está deshabilitado (matchIfMissing = false).
 * Este módulo no expone endpoints REST propios; proporciona servicios internos
 * de notificación por email utilizados por otros módulos del sistema.
 */
@Configuration

public class NotificationModuleConfig {
}
