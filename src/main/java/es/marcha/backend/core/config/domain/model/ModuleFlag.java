package es.marcha.backend.core.config.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa el estado de habilitación de un módulo de negocio.
 * Se persiste en base de datos para mantener la configuración entre reinicios.
 */
@Entity
@Table(name = "module_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del módulo (cart, catalog, order, etc.)
     */
    @Column(name = "module_name", nullable = false, unique = true, length = 50)
    private String moduleName;

    /**
     * Estado de habilitación del módulo.
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
}
