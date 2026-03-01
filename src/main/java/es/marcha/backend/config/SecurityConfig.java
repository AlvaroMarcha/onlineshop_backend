package es.marcha.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import es.marcha.backend.security.JwtFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Codificador de contraseñas BCrypt con coste 12 (recomendación OWASP).
     * Inyectar este bean en cualquier servicio que necesite codificar o verificar
     * contraseñas.
     *
     * @return instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Cadena de seguridad para el entorno de pruebas (Live Server :5500).
     * Permite todas las peticiones sin autenticación.
     * Se evalúa primero gracias a {@code @Order(1)}.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(request -> {
                    String origin = request.getHeader("Origin");
                    return origin != null
                            && (origin.equals("http://localhost:5500")
                                    || origin.equals("http://127.0.0.1:5500"));
                })
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * Cadena de seguridad para herramientas de testing sin origen (Postman, curl,
     * etc.).
     * Permite todas las peticiones sin autenticación cuando no se envía cabecera
     * {@code Origin}.
     * Se evalúa en segundo lugar gracias a {@code @Order(2)}.
     *
     * <p>
     * ⚠️ Eliminar o deshabilitar en producción.
     * </p>
     */
    @Bean
    @Order(2)
    public SecurityFilterChain noTokenFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(request -> request.getHeader("Origin") == null)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * Cadena de seguridad para el front real (Angular :4200) y cualquier otro
     * origen.
     * Requiere JWT con rol válido. Las reglas se evalúan en orden:
     *
     * <ul>
     * <li>Públicas: {@code /auth/**}, {@code /images/**}, {@code /health/**}</li>
     * <li>{@code /roles/**} — solo {@code ROLE_SUPER_ADMIN}</li>
     * <li>Escritura tienda — {@code ROLE_SUPER_ADMIN}, {@code ROLE_ADMIN},
     * {@code ROLE_STORE}</li>
     * <li>Pedidos — {@code ROLE_SUPER_ADMIN}, {@code ROLE_ADMIN},
     * {@code ROLE_ORDERS}, {@code ROLE_USER}</li>
     * <li>Facturas — {@code ROLE_SUPER_ADMIN}, {@code ROLE_ADMIN},
     * {@code ROLE_CUSTOMERS_INVOICES}, {@code ROLE_USER}</li>
     * <li>Usuarios — {@code ROLE_SUPER_ADMIN}, {@code ROLE_ADMIN},
     * {@code ROLE_CUSTOMERS_INVOICES}, {@code ROLE_SUPPORT}</li>
     * <li>Lectura tienda y reseñas — cualquier autenticado</li>
     * </ul>
     *
     * Se evalúa en tercer lugar gracias a {@code @Order(3)}.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // === Rutas públicas ===
                        .requestMatchers("/auth/**", "/images/**", "/health/**").permitAll()

                        // === Gestión de roles: solo SUPER_ADMIN ===
                        .requestMatchers("/roles/**").hasRole("SUPER_ADMIN")

                        // === Escritura en tienda (productos, categorías, subcategorías, atributos,
                        // variantes) ===
                        .requestMatchers(HttpMethod.POST,
                                "/products/**", "/categories/**", "/subcategories/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "STORE")
                        .requestMatchers(HttpMethod.PUT,
                                "/products/**", "/categories/**", "/subcategories/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "STORE")
                        .requestMatchers(HttpMethod.DELETE,
                                "/products/**", "/categories/**", "/subcategories/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "STORE")

                        // === Lectura tienda y reseñas: cualquier usuario autenticado ===
                        .requestMatchers(HttpMethod.GET,
                                "/products/**", "/categories/**", "/subcategories/**")
                        .authenticated()
                        .requestMatchers("/reviews/**").authenticated()

                        // === Pedidos ===
                        .requestMatchers("/orders/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "ORDERS", "USER")

                        // === Facturas ===
                        .requestMatchers("/invoices/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMERS_INVOICES", "USER")

                        // === Gestión de usuarios ===
                        .requestMatchers("/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMERS_INVOICES", "SUPPORT")

                        // === Resto de endpoints: solo administradores ===
                        .anyRequest().hasAnyRole("SUPER_ADMIN", "ADMIN"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
