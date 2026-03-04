package es.marcha.backend.core.config.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.core.config.domain.model.ModuleFlag;

@Repository
public interface ModuleFlagRepository extends JpaRepository<ModuleFlag, Long> {

    /**
     * Busca la configuración de un módulo específico por su nombre.
     */
    Optional<ModuleFlag> findByModuleName(String moduleName);

    /**
     * Verifica si existe una configuración para un módulo específico.
     */
    boolean existsByModuleName(String moduleName);
}
