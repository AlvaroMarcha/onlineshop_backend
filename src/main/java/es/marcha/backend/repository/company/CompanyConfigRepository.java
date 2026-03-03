package es.marcha.backend.repository.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.company.CompanyConfig;

/**
 * Repositorio JPA para la configuración de empresa.
 * La tabla {@code company_config} contiene siempre exactamente 1 fila (id = 1).
 */
@Repository
public interface CompanyConfigRepository extends JpaRepository<CompanyConfig, Long> {
}
