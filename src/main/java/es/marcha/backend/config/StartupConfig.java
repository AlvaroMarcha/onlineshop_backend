package es.marcha.backend.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.marcha.backend.model.enums.RoleName;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.repository.user.RoleRepository;

@Component
public class StartupConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupConfig.class);

    private static final String DEFAULT_PIC_PROFILE = "default_pic_profile.jpeg";

    /** Carpeta reservada para los recursos por defecto */
    private static final String DEFAULT_FOLDER = "default";

    private final Path destinationDir;
    private final RoleRepository roleRepository;

    private Path exportPath;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.images.public-path}")
    private String imagesPublicPath;

    public StartupConfig(
            @Value("${app.images.storage-path}") String path,
            RoleRepository roleRepository) {
        this.destinationDir = Paths.get(path).toAbsolutePath().normalize();
        this.roleRepository = roleRepository;
    }

    /**
     * Método ejecutado automáticamente por Spring Boot al arrancar la aplicación.
     *
     * <p>
     * Su responsabilidad es:
     * <ul>
     * <li>Crear el directorio de destino para imágenes de perfil si no existe.</li>
     * <li>Localizar la imagen de perfil por defecto dentro de los recursos
     * estáticos
     * del classpath ({@code static/default/}).</li>
     * <li>Copiar dicha imagen al directorio de destino configurado.</li>
     * </ul>
     *
     * <p>
     * Si el archivo de origen no existe en el classpath, el proceso se interrumpe
     * sin lanzar excepción para evitar que la aplicación falle durante el arranque.
     *
     * @param args argumentos de línea de comandos proporcionados por Spring Boot
     */
    @Override
    public void run(String... args) {
        seedRoles();
        try {
            Path defaultDir = destinationDir.resolve(DEFAULT_FOLDER);
            if (Files.notExists(defaultDir)) {
                Files.createDirectories(defaultDir);
            }

            exportPath = defaultDir.resolve(DEFAULT_PIC_PROFILE);

            ClassPathResource resource = new ClassPathResource("static/default/" + DEFAULT_PIC_PROFILE);

            if (!resource.exists()) {
                log.error("FILE_NOT_FOUND en classpath: static/default/{}", DEFAULT_PIC_PROFILE);
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, exportPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            log.error("Error al copiar la imagen de perfil por defecto", e);
        }
    }

    /**
     * Siembra los roles del sistema en base de datos si no existen.
     * Se ejecuta en cada arranque de la aplicación, de modo que es seguro
     * en entornos sin migraciones automáticas.
     */
    private void seedRoles() {
        Map<String, String> roles = new LinkedHashMap<>();
        roles.put(RoleName.ROLE_SUPER_ADMIN.name(), "Control total del sistema");
        roles.put(RoleName.ROLE_ADMIN.name(), "Administración general");
        roles.put(RoleName.ROLE_ORDERS.name(), "Gestión de pedidos");
        roles.put(RoleName.ROLE_CUSTOMERS_INVOICES.name(), "Gestión de clientes y facturas");
        roles.put(RoleName.ROLE_SUPPORT.name(), "Atención al cliente");
        roles.put(RoleName.ROLE_STORE.name(), "Gestión de productos y tienda");
        roles.put(RoleName.ROLE_USER.name(), "Cliente registrado");

        roles.forEach((name, description) -> {
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(Role.builder()
                        .name(name)
                        .description(description)
                        .createdAt(LocalDateTime.now())
                        .build());
                log.info("Rol creado: {}", name);
            }
        });
    }

    /**
     * Construye y devuelve la URL pública de la imagen de perfil por defecto,
     * compuesta por la base URL, la ruta pública de imágenes y el nombre del
     * archivo.
     *
     * @return URL pública de la imagen de perfil por defecto
     */
    public String getDefaultProfileImageUrl() {
        return buildImageUrl(DEFAULT_PIC_PROFILE, DEFAULT_FOLDER);
    }

    private String buildImageUrl(String filename, String folder) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = imagesPublicPath.startsWith("/") ? imagesPublicPath : "/" + imagesPublicPath;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return base + path + "/" + folder + "/" + filename;
    }
}
