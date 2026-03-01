package es.marcha.backend.services.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.exception.MediaException;
import es.marcha.backend.model.user.User;
import es.marcha.backend.services.user.UserService;

@Service
public class MediaService {

    private static final String DEFAULT_PIC_PROFILE = "default_pic_profile.jpeg";

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/x-png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    // Magic bytes: JPEG = FF D8 FF | PNG = 89 50 4E 47
    private static final byte[] MAGIC_JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] MAGIC_PNG = { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 };

    @Value("${app.images.storage-path}")
    private String storagePath;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.images.public-path}")
    private String imagesPublicPath;

    private final UserService uService;

    public MediaService(@Lazy UserService uService) {
        this.uService = uService;
    }

    public String newPicProfile(MultipartFile file, long id) {
        User user = uService.getUserByIdForHandler(id);
        String username = user.getUsername();

        if (file.isEmpty()) {
            throw new MediaException();
        }

        // 1. Validar MIME type declarado
        String contentType = file.getContentType();
        String normalizedType = contentType != null ? contentType.split(";")[0].trim().toLowerCase() : null;
        if (normalizedType == null || !ALLOWED_MIME_TYPES.contains(normalizedType)) {
            throw new MediaException(MediaException.INVALID_FILE_TYPE);
        }

        // 2. Validar extensión del nombre de archivo
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new MediaException(MediaException.INVALID_FILE_TYPE);
        }

        // 3. Validar magic bytes (previene spoofing de MIME)
        validateMagicBytes(file);

        String fileName = username + "_profile" + extension;

        Path userPicDir = Path.of(storagePath).resolve(String.valueOf(id)).resolve("pic-profile");
        Path target = userPicDir.resolve(fileName);

        try {
            if (Files.notExists(userPicDir)) {
                Files.createDirectories(userPicDir);
            }
        } catch (IOException e) {
            throw new MediaException(MediaException.STORAGE_ERROR, e);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new MediaException(MediaException.STORAGE_ERROR, e);
        }

        user.setProfileImageUrl(buildImageUrl(fileName, id));
        uService.saveUserForHandler(user);
        return buildImageUrl(fileName, id);
    }

    public String getDefaultPicProfile() {
        return DEFAULT_PIC_PROFILE;
    }

    /**
     * Devuelve la URL pública de la imagen de perfil por defecto, apuntando
     * a la carpeta compartida {@code default} del servidor de imágenes.
     *
     * @return URL de la imagen de perfil por defecto
     */
    public String getDefaultProfileImageUrl() {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = imagesPublicPath.startsWith("/") ? imagesPublicPath : "/" + imagesPublicPath;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return base + path + "/default/" + DEFAULT_PIC_PROFILE;
    }

    /**
     * Verifica que los bytes iniciales del archivo coincidan con una firma
     * conocida de imagen (JPEG o PNG), evitando que se engañe al servidor
     * mediante un MIME type falsificado.
     *
     * @param file archivo recibido en la petición
     * @throws MediaException si el contenido real no coincide con ninguna firma
     *                        válida
     */
    public void validateMagicBytes(MultipartFile file) {
        byte[] header;
        try {
            byte[] allBytes = file.getBytes();
            if (allBytes.length < 4) {
                throw new MediaException(MediaException.INVALID_FILE_CONTENT);
            }
            header = allBytes;
        } catch (MediaException e) {
            throw e;
        } catch (IOException e) {
            throw new MediaException(MediaException.INVALID_FILE_CONTENT, e);
        }

        if (startsWith(header, MAGIC_JPEG) || startsWith(header, MAGIC_PNG)) {
            return;
        }

        throw new MediaException(MediaException.INVALID_FILE_CONTENT);
    }

    public boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length)
            return false;
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i])
                return false;
        }
        return true;
    }

    public String buildImageUrl(String filename, long userId) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = imagesPublicPath.startsWith("/") ? imagesPublicPath : "/" + imagesPublicPath;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return base + path + "/" + userId + "/pic-profile/" + filename;
    }

    /**
     * Devuelve el archivo del logotipo de la empresa como
     * {@link FileSystemResource},
     * buscando {@code company/logo} con extensiones .png, .jpg y .jpeg.
     *
     * @return {@code Optional} con el recurso si existe, vacío si no se ha subido
     *         logo
     */
    public Optional<FileSystemResource> getCompanyLogoResource() {
        for (String ext : List.of(".png", ".jpg", ".jpeg")) {
            Path logoPath = Path.of(storagePath).resolve("company").resolve("logo" + ext);
            if (Files.exists(logoPath)) {
                return Optional.of(new FileSystemResource(logoPath));
            }
        }
        return Optional.empty();
    }

    /**
     * Devuelve la URL pública del logotipo de la empresa actualmente almacenado.
     * Busca el archivo {@code company/logo} con extensiones .png, .jpg o .jpeg
     * y devuelve la URL pública correspondiente cuando lo encuentra.
     *
     * @return URL pública del logo, o cadena vacía si no se ha subido ningún logo
     */
    public String getCompanyLogoUrl() {
        for (String ext : List.of(".png", ".jpg", ".jpeg")) {
            Path logoPath = Path.of(storagePath).resolve("company").resolve("logo" + ext);
            if (Files.exists(logoPath)) {
                String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                String path = imagesPublicPath.startsWith("/") ? imagesPublicPath : "/" + imagesPublicPath;
                if (path.endsWith("/"))
                    path = path.substring(0, path.length() - 1);
                return base + path + "/company/logo" + ext;
            }
        }
        return "";
    }

    /**
     * Guarda o reemplaza el logotipo de la empresa en disco.
     *
     * El archivo se almacena en {@code {storagePath}/company/logo.{ext}}.
     * Si ya existía un logo previo con distinta extensión, convive en disco;
     * el nombre fijo {@code logo.{ext}} garantiza que la ruta es predecible.
     *
     * @param file imagen del logo (JPEG o PNG)
     * @return ruta absoluta en disco donde quedó guardado el archivo
     * @throws MediaException si el archivo está vacío, tiene un tipo no permitido
     *                        o no supera la validación de magic bytes
     */
    public String saveCompanyLogo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new MediaException();
        }

        String contentType = file.getContentType();
        String normalizedType = contentType != null ? contentType.split(";")[0].trim().toLowerCase() : null;
        if (normalizedType == null || !ALLOWED_MIME_TYPES.contains(normalizedType)) {
            throw new MediaException(MediaException.INVALID_FILE_TYPE);
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new MediaException(MediaException.INVALID_FILE_TYPE);
        }

        validateMagicBytes(file);

        Path companyDir = Path.of(storagePath).resolve("company");
        Path target = companyDir.resolve("logo" + extension);

        try {
            if (Files.notExists(companyDir)) {
                Files.createDirectories(companyDir);
            }
        } catch (IOException e) {
            throw new MediaException(MediaException.STORAGE_ERROR, e);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new MediaException(MediaException.STORAGE_ERROR, e);
        }

        return target.toAbsolutePath().toString();
    }

    public String getExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return ".jpg";
        }
        return originalName.substring(originalName.lastIndexOf("."));
    }

}
