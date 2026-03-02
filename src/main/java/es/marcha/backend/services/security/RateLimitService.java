package es.marcha.backend.services.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.marcha.backend.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    public enum EndpointType {
        LOGIN,
        REGISTER,
        PASSWORD_RESET
    }

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Intenta consumir un token para la IP y el tipo de endpoint dados.
     * Si el bucket está agotado, lanza {@link RateLimitException} con el
     * tiempo de espera en segundos para el header {@code Retry-After}.
     *
     * @param ip   La IP del cliente (puede ser X-Forwarded-For o remoteAddr)
     * @param type El tipo de endpoint que se está accediendo
     * @throws RateLimitException si se ha superado el límite de intentos
     */
    public void checkRateLimit(String ip, EndpointType type) {
        String key = buildKey(ip, type);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(type));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
            log.warn("Rate limit superado — IP: {}, endpoint: {}, retry-after: {}s", ip, type, retryAfterSeconds);
            throw new RateLimitException(retryAfterSeconds);
        }

        log.debug("Rate limit OK — IP: {}, endpoint: {}, tokens restantes: {}",
                ip, type, probe.getRemainingTokens());
    }

    /**
     * Resetea el contador de intentos para una IP y endpoint dados.
     * Se debe llamar cuando una petición finaliza con éxito para que
     * el cliente pueda comenzar con el contador limpio.
     *
     * @param ip   La IP del cliente
     * @param type El tipo de endpoint
     */
    public void resetCounter(String ip, EndpointType type) {
        String key = buildKey(ip, type);
        buckets.remove(key);
        log.debug("Contador de rate limit reseteado — IP: {}, endpoint: {}", ip, type);
    }

    /**
     * Construye la clave única del bucket como "IP:ENDPOINT_TYPE".
     */
    private String buildKey(String ip, EndpointType type) {
        return ip + ":" + type.name();
    }

    /**
     * Crea un nuevo bucket con la configuración correspondiente al tipo de
     * endpoint.
     *
     * @param type El tipo de endpoint
     * @return Bucket configurado con la capacidad y ventana temporal adecuadas
     */
    private Bucket createBucket(EndpointType type) {
        Bandwidth limit = switch (type) {
            case LOGIN -> Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
            case REGISTER -> Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)));
            case PASSWORD_RESET -> Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
        };

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
