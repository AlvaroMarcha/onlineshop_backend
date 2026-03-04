package es.marcha.backend.modules.company.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuración de empresa almacenada en base de datos.
 * <p>
 * Esta tabla contiene siempre exactamente <b>1 fila</b> con {@code id = 1}.
 * Si no existe ningún registro, el {@code CompanyService} hace fallback a los
 * valores de {@code application.properties} (leídos via
 * {@code CompanyProperties}).
 * </p>
 * <p>
 * Permite al SUPER_ADMIN actualizar los datos de empresa en tiempo de ejecución
 * sin necesidad de modificar el {@code .env} ni reiniciar el servidor.
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "company_config")
public class CompanyConfig {

    /** Siempre 1 — registro único de configuración de empresa. */
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "nif")
    private String nif;
    @Column(name = "address")
    private String address;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "iban")
    private String iban;
    @Column(name = "primary_color")
    private String primaryColor;
    @Column(name = "secondary_color")
    private String secondaryColor;
    @Column(name = "accent_color")
    private String accentColor;
    @Column(name = "text_color")
    private String textColor;
    @Column(name = "logo_path")
    private String logoPath;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
