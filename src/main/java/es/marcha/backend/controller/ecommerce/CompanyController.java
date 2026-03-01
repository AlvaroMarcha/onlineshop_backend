package es.marcha.backend.controller.ecommerce;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.services.media.MediaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/company")
public class CompanyController {

    private final MediaService mService;

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
