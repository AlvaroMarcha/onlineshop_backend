package es.marcha.backend.controller.ecommerce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.dto.response.company.CompanyConfigDTO;
import es.marcha.backend.services.company.CompanyService;
import es.marcha.backend.services.media.MediaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/company")
public class CompanyController {

    private final MediaService mService;

    @Autowired
    private CompanyService companyService;

    /**
     * Obtiene la configuración actual de empresa.
     * Si no existe ningún registro en BD, devuelve los valores del {@code .env}.
     *
     * @return {@link ResponseEntity} con {@link CompanyConfigDTO} y código HTTP 200
     *         OK
     */
    @GetMapping
    public ResponseEntity<CompanyConfigDTO> getCompanyConfig() {
        CompanyConfigDTO config = companyService.getConfig();
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    /**
     * Actualiza la configuración de empresa en la base de datos.
     * Los campos no enviados ({@code null}) conservan su valor actual.
     *
     * @param dto DTO con los datos a actualizar
     * @return {@link ResponseEntity} con la configuración actualizada y código HTTP
     *         200 OK
     */
    @PutMapping
    public ResponseEntity<CompanyConfigDTO> updateCompanyConfig(@RequestBody CompanyConfigDTO dto) {
        CompanyConfigDTO updated = companyService.updateConfig(dto);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Sube o reemplaza el logotipo de la empresa.
     *
     * Guarda el archivo en {@code {IMAGES_STORAGE_PATH}/company/logo.{ext}} y
     * devuelve la ruta absoluta en disco donde quedó almacenado. Esta ruta es
     * la que debe configurarse en la variable de entorno {@code COMPANY_LOGO_PATH}.
     *
     * Solo acepta JPEG y PNG. La validación incluye MIME type, extensión
     * y magic bytes para prevenir spoofing.
     *
     * @param file imagen del logo (campo {@code file}, {@code multipart/form-data})
     * @return ruta absoluta del logo guardado en disco
     */
    @PostMapping("/logo")
    public ResponseEntity<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        String savedPath = mService.saveCompanyLogo(file);
        return ResponseEntity.ok(savedPath);
    }
}
