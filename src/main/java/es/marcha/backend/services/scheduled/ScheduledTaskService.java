package es.marcha.backend.services.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import es.marcha.backend.services.security.RefreshTokenService;

@Service
public class ScheduledTaskService {

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Elimina los refresh tokens expirados o revocados de la base de datos.
     * Se ejecuta una vez al día a medianoche.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanExpiredRefreshTokens() {
        refreshTokenService.deleteExpiredTokens();
    }
}
