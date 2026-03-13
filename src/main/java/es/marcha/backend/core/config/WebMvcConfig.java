package es.marcha.backend.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        @Value("${app.images.public-path}")
        private String imagesPublicPath;

        @Value("${app.images.storage-path}")
        private String imagesStoragePath;

        private final ModuleFlagInterceptor moduleFlagInterceptor;

        @Autowired
        public WebMvcConfig(ModuleFlagInterceptor moduleFlagInterceptor) {
                this.moduleFlagInterceptor = moduleFlagInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(moduleFlagInterceptor);
        }

        /**
         * Mapea las solicitudes a la ruta pública de imágenes hacia el directorio
         * del sistema de ficheros donde se almacenan las imágenes subidas por los
         * usuarios y los recursos por defecto.
         *
         * <p>
         * Ejemplo: {@code GET /images/42/pic-profile/john_profile.jpg}
         * → {@code C:/uploads/images/42/pic-profile/john_profile.jpg}
         * </p>
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String pathPattern = imagesPublicPath.endsWith("/")
                                ? imagesPublicPath + "**"
                                : imagesPublicPath + "/**";

                String fileSystemPath = imagesStoragePath.endsWith("/")
                                ? "file:" + imagesStoragePath
                                : "file:" + imagesStoragePath + "/";

                registry.addResourceHandler(pathPattern)
                                .addResourceLocations(fileSystemPath);
        }
}
