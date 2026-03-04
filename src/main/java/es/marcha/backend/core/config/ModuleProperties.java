package es.marcha.backend.core.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.marcha.backend.core.config.domain.model.ModuleFlag;
import es.marcha.backend.core.config.infrastructure.persistence.ModuleFlagRepository;
import jakarta.annotation.PostConstruct;

/**
 * Bean que gestiona el estado de habilitación de cada módulo de negocio.
 *
 * <p>Los valores se cargan desde la base de datos al iniciar la aplicación y se
 * mantienen sincronizados. Cada cambio se persiste inmediatamente.</p>
 *
 * <p>Si un módulo no existe en la BD, se crea automáticamente con valor {@code false}.</p>
 */
@Component
public class ModuleProperties {

    private static final List<String> MODULE_NAMES = Arrays.asList(
            "cart", "catalog", "company", "coupon",
            "invoice", "notification", "order", "wishlist"
    );

    private final ModuleFlagRepository repository;
    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    @Autowired
    public ModuleProperties(ModuleFlagRepository repository) {
        this.repository = repository;
    }

    /**
     * Inicializa el cache cargando los valores desde la base de datos.
     * Si algún módulo no existe, lo crea con valor false.
     */
    @PostConstruct
    public void init() {
        for (String moduleName : MODULE_NAMES) {
            ModuleFlag flag = repository.findByModuleName(moduleName)
                    .orElseGet(() -> {
                        ModuleFlag newFlag = ModuleFlag.builder()
                                .moduleName(moduleName)
                                .enabled(false)
                                .build();
                        return repository.save(newFlag);
                    });
            cache.put(moduleName, flag.getEnabled());
        }
    }

    /**
     * Consulta si un módulo está habilitado.
     */
    public boolean isEnabled(String module) {
        return cache.getOrDefault(module, false);
    }

    /**
     * Cambia el estado de un módulo y lo persiste en base de datos.
     */
    public void setEnabled(String module, boolean value) {
        if (!MODULE_NAMES.contains(module)) {
            return; // módulo no conocido, ignorar
        }

        cache.put(module, value);

        // Persistir en BD
        ModuleFlag flag = repository.findByModuleName(module)
                .orElseGet(() -> ModuleFlag.builder()
                        .moduleName(module)
                        .enabled(value)
                        .build());
        flag.setEnabled(value);
        repository.save(flag);
    }

    /**
     * Devuelve un mapa con los valores actuales de todos los módulos.
     */
    public Map<String, Boolean> asMap() {
        return MODULE_NAMES.stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> cache.getOrDefault(name, false)
                ));
    }
}
