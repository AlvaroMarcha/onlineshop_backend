package es.marcha.backend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita el soporte de tareas programadas con @Scheduled en el contexto de
 * Spring.
 * Todas las tareas periódicas se centralizan en ScheduledTaskService.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
