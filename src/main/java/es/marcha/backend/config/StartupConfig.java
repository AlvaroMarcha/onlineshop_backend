package es.marcha.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupConfig implements CommandLineRunner {

    private final Path destination_dir;

    private final String default_pic_profile =
            "default_pic_profile.jpeg";

    private Path export_path;


    public StartupConfig(@Value("${IMAGES_STORAGE_PATH}") String path) {
        // Solo convertimos String a Path
        this.destination_dir = Paths.get(path);
    }

    /**
     * Método ejecutado automáticamente por Spring Boot al arrancar la aplicación.
     *
     * <p>
     * Su responsabilidad es:
     * <ul>
     * <li>Crear el directorio de destino para imágenes de perfil si no existe.</li>
     * <li>Localizar la imagen de perfil por defecto en la carpeta {@code Pictures}
     * del usuario del sistema.</li>
     * <li>Copiar dicha imagen al directorio de destino configurado.</li>
     * </ul>
     *
     * <p>
     * Si el archivo de origen no existe, el proceso se interrumpe sin lanzar excepción
     * para evitar que la aplicación falle durante el arranque.
     *
     * @param args argumentos de línea de comandos proporcionados por Spring Boot
     */
    @Override
    public void run(String... args) {
        try {
            if (Files.notExists(destination_dir)) {
                Files.createDirectories(destination_dir);
            }

            export_path = destination_dir.resolve(default_pic_profile);

            Path sourcePath = Paths.get(
                    System.getProperty("user.home"),
                    "Pictures",
                    default_pic_profile);

            if (Files.notExists(sourcePath)) {
                System.err.println("Archivo NO encontrado: " + sourcePath);
                return;
            }

            Files.copy(sourcePath, export_path, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Devuelve la ruta absoluta donde se almacena la imagen de perfil por defecto
     * dentro del sistema de archivos.
     *
     * <p>
     * Este método es estático para permitir el acceso a la ruta desde otras partes
     * de la aplicación sin necesidad de instanciar la clase.
     *
     * @return {@link Path} absoluto del archivo de imagen de perfil por defecto
     */
    public Path getDestinationPathURLImage() {
        return export_path;
    }
}
