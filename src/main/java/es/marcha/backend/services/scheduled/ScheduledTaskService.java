package es.marcha.backend.services.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import es.marcha.backend.services.cart.CartService;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio centralizado de tareas programadas.
 * <p>
 * Toda tarea periódica del sistema debe registrarse aquí para mantener
 * un único punto de entrada escalable. La lógica de negocio permanece
 * en sus servicios respectivos; este servicio solo orquesta la ejecución.
 * </p>
 */
@Service
@Slf4j
public class ScheduledTaskService {

    @Autowired
    private CartService cartService;

    /**
     * Expira automáticamente los carritos inactivos.
     * <p>
     * Se ejecuta cada 30 minutos. Marca como EXPIRED todos los carritos
     * activos cuya {@code expiresAt} ya ha pasado (2h sin actividad).
     * </p>
     */
    @Scheduled(fixedRate = 1_800_000) // cada 30 minutos
    public void expireInactiveCarts() {
        log.info("[Scheduler] Iniciando expiración de carritos inactivos...");
        cartService.expireOldCarts();
        log.info("[Scheduler] Expiración de carritos completada.");
    }
}
