package es.marcha.backend.services.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.exception.MediaException;
import es.marcha.backend.model.user.User;
import es.marcha.backend.services.user.UserService;

@Service
public class MediaService {

    private static final String DEFAULT_PIC_PROFILE = "default_pic_profile.jpeg";

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png");
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
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
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
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            if (is.read(header) < 4) {
                throw new MediaException(MediaException.INVALID_FILE_CONTENT);
            }
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

    public String getExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return ".jpg";
        }
        return originalName.substring(originalName.lastIndexOf("."));
    }

}
