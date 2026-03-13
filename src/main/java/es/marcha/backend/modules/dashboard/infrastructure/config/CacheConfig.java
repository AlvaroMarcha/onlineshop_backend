package es.marcha.backend.modules.dashboard.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configuración de caché para el módulo Dashboard.
 * <p>
 * Utiliza Caffeine como proveedor de caché en memoria con TTL configurable
 * para cada tipo de métrica. Los cachés configurados son:
 * </p>
 * <ul>
 * <li><b>dashboardRevenue</b>: 10 minutos - Ingresos totales por periodo</li>
 * <li><b>dashboardRevenueChart</b>: 15 minutos - Datos de gráficas de
 * ingresos</li>
 * <li><b>dashboardOrderStats</b>: 5 minutos - Estadísticas de pedidos</li>
 * <li><b>dashboardUserStats</b>: 10 minutos - Estadísticas de usuarios</li>
 * <li><b>dashboardTopSelling</b>: 10 minutos - Productos más vendidos</li>
 * <li><b>dashboardLowStock</b>: 5 minutos - Productos con stock bajo</li>
 * <li><b>dashboardConversion</b>: 15 minutos - Tasa de conversión</li>
 * <li><b>dashboardAOV</b>: 10 minutos - Valor medio del pedido</li>
 * <li><b>dashboardRecentInvoices</b>: 5 minutos - Facturas recientes</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Crea el CacheManager global de la aplicación usando Caffeine.
     * <p>
     * Por defecto, todos los cachés tienen TTL de 10 minutos y máximo 1000
     * entradas. Para configuraciones específicas, usar
     * {@link #dashboardCacheManager()}.
     * </p>
     *
     * @return CacheManager configurado con Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }

    /**
     * CacheManager específico para el Dashboard con TTLs ajustados por métrica.
     * <p>
     * Este bean no se usa actualmente porque Caffeine no soporta TTL diferente
     * por caché en el mismo manager. Para diferenciar TTL por caché, se
     * necesitaría un CacheManager por caché.
     * </p>
     * <p>
     * Como solución práctica, se usa un TTL universal de 10 minutos. Si se
     * necesitan TTLs específicos, considerar migrar a Redis.
     * </p>
     *
     * @return CacheManager configurado para Dashboard
     */
    // @Bean
    // public CacheManager dashboardCacheManager() {
    // CaffeineCacheManager cacheManager = new CaffeineCacheManager(
    // "dashboardRevenue",
    // "dashboardRevenueChart",
    // "dashboardOrderStats",
    // "dashboardUserStats",
    // "dashboardTopSelling",
    // "dashboardLowStock",
    // "dashboardConversion",
    // "dashboardAOV",
    // "dashboardRecentInvoices");
    //
    // cacheManager.setCaffeine(Caffeine.newBuilder()
    // .expireAfterWrite(10, TimeUnit.MINUTES)
    // .maximumSize(500)
    // .recordStats());
    // return cacheManager;
    // }
}
