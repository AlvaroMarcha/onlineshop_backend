package es.marcha.backend.services.company;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.config.CompanyProperties;
import es.marcha.backend.dto.response.company.CompanyConfigDTO;
import es.marcha.backend.model.company.CompanyConfig;
import es.marcha.backend.repository.company.CompanyConfigRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de configuración de empresa.
 * <p>
 * Si existe una fila en la tabla {@code company_config} (id = 1), se usan esos
 * valores. Si no, se hace fallback a {@code application.properties}
 * (leídos vía {@link CompanyProperties}).
 * </p>
 * <p>
 * {@code PUT /company} persiste o actualiza la fila de BD. Tras la primera
 * actualización, el fallback a properties deja de aplicarse.
 * </p>
 */
@Service
public class CompanyService {

    /** ID fijo del único registro de configuración de empresa en BD. */
    private static final long CONFIG_ID = 1L;

    @Autowired
    private CompanyConfigRepository companyConfigRepository;

    @Autowired
    private CompanyProperties companyProperties;

    // ─── Lectura ─────────────────────────────────────────────────────────────

    /**
     * Obtiene la configuración de empresa activa.
     * Prioriza el registro de BD sobre los valores del {@code .env}.
     *
     * @return {@link CompanyConfigDTO} con los datos de empresa
     */
    @Transactional(readOnly = true)
    public CompanyConfigDTO getConfig() {
        return companyConfigRepository.findById(CONFIG_ID)
                .map(this::toDTO)
                .orElseGet(this::fromProperties);
    }

    // ─── Escritura ───────────────────────────────────────────────────────────

    /**
     * Crea o actualiza la configuración de empresa en BD.
     * Si no existía ningún registro, lo crea con id = 1.
     * Los campos {@code null} en el DTO mantienen el valor actual en BD
     * (o el de properties si es la primera vez).
     *
     * @param dto DTO con los nuevos datos a persistir
     * @return {@link CompanyConfigDTO} con los valores persistidos
     */
    @Transactional
    public CompanyConfigDTO updateConfig(CompanyConfigDTO dto) {
        // Cargar el estado actual (BD o properties) para no perder campos no enviados
        CompanyConfigDTO current = getConfig();

        CompanyConfig config = companyConfigRepository.findById(CONFIG_ID)
                .orElse(CompanyConfig.builder().id(CONFIG_ID).build());

        config.setName(dto.getName() != null ? dto.getName() : current.getName());
        config.setNif(dto.getNif() != null ? dto.getNif() : current.getNif());
        config.setAddress(dto.getAddress() != null ? dto.getAddress() : current.getAddress());
        config.setEmail(dto.getEmail() != null ? dto.getEmail() : current.getEmail());
        config.setPhone(dto.getPhone() != null ? dto.getPhone() : current.getPhone());
        config.setIban(dto.getIban() != null ? dto.getIban() : current.getIban());
        config.setPrimaryColor(dto.getPrimaryColor() != null ? dto.getPrimaryColor() : current.getPrimaryColor());
        config.setSecondaryColor(
                dto.getSecondaryColor() != null ? dto.getSecondaryColor() : current.getSecondaryColor());
        config.setAccentColor(dto.getAccentColor() != null ? dto.getAccentColor() : current.getAccentColor());
        config.setTextColor(dto.getTextColor() != null ? dto.getTextColor() : current.getTextColor());
        config.setLogoPath(dto.getLogoPath() != null ? dto.getLogoPath() : current.getLogoPath()); // path/url del logo
        config.setUpdatedAt(LocalDateTime.now());

        return toDTO(companyConfigRepository.save(config));
    }

    // ─── Mapeo privado ────────────────────────────────────────────────────────

    /**
     * Mapea la entidad {@link CompanyConfig} a DTO.
     */
    private CompanyConfigDTO toDTO(CompanyConfig config) {
        return CompanyConfigDTO.builder()
                .name(config.getName())
                .nif(config.getNif())
                .address(config.getAddress())
                .email(config.getEmail())
                .phone(config.getPhone())
                .iban(config.getIban())
                .primaryColor(config.getPrimaryColor())
                .secondaryColor(config.getSecondaryColor())
                .accentColor(config.getAccentColor())
                .textColor(config.getTextColor())
                .logoPath(config.getLogoPath())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    /**
     * Construye el DTO a partir de los valores del {@code .env} vía
     * {@link CompanyProperties}. Usado como fallback cuando no hay registro en BD.
     */
    private CompanyConfigDTO fromProperties() {
        return CompanyConfigDTO.builder()
                .name(companyProperties.getName())
                .nif(companyProperties.getNif())
                .address(companyProperties.getAddress())
                .email(companyProperties.getEmail())
                .phone(companyProperties.getPhone())
                .iban(companyProperties.getIban())
                .primaryColor(companyProperties.getPrimaryColor())
                .secondaryColor(companyProperties.getSecondaryColor())
                .accentColor(companyProperties.getAccentColor())
                .textColor(companyProperties.getTextColor())
                .logoPath(companyProperties.getLogoPath())
                .updatedAt(null)
                .build();
    }
}
