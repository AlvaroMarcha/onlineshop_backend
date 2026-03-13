package es.marcha.backend.core.auth.infrastructure.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        /**
         * Orígenes permitidos cargados desde la variable de entorno
         * {@code CORS_ALLOWED_ORIGINS}.
         * Soporta patrones con wildcard (p. ej.
         * {@code http://localhost:*,https://miapp-*.vercel.app}).
         */
        @Value("${app.cors.allowed-origin-patterns}")
        private String allowedOriginPatterns;

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
        private final VerifiedUserFilter verifiedUserFilter;

        public SecurityConfig(JwtFilter jwtFilter, VerifiedUserFilter verifiedUserFilter) {
                this.jwtFilter = jwtFilter;
                this.verifiedUserFilter = verifiedUserFilter;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                // Parsear los patrones de orígenes permitidos desde la configuración
                List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();

                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(patterns);
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * Cadena de seguridad para el entorno de desarrollo local (localhost /
         * 127.0.0.1).
         * Permite todas las peticiones sin autenticación desde cualquier puerto local.
         * Se evalúa primero gracias a {@code @Order(1)}.
         */
        @Bean
        @Order(1)
        public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
                return http
                                .securityMatcher(request -> {
                                        String origin = request.getHeader("Origin");
                                        return origin != null
                                                        && (origin.startsWith("http://localhost:")
                                                                        || origin.startsWith("http://127.0.0.1:"));
                                })
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth

                                                // === Rutas públicas ===
                                                .requestMatchers("/auth/**", "/images/**", "/health/**").permitAll()

                                                // === Stripe webhook: público — Stripe no puede enviar JWT ===
                                                .requestMatchers(HttpMethod.POST, "/stripe/webhook").permitAll()

                                                // === Wishlist: contador público de producto ===
                                                .requestMatchers(HttpMethod.GET, "/wishlist/product/*/count")
                                                .permitAll()

                                                // === Gestión de roles: solo SUPER_ADMIN ===
                                                .requestMatchers("/roles/**").hasRole("SUPER_ADMIN")

                                                // === Configuración de empresa (logo, etc.) ===
                                                .requestMatchers("/company/**").hasRole("SUPER_ADMIN")

                                                // === Escritura en tienda (productos, categorías, subcategorías,
                                                // atributos,
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

                                                // === Stripe pagos: crear PaymentIntent ===
                                                .requestMatchers("/stripe/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "ORDERS", "USER")

                                                // === Facturas ===
                                                .requestMatchers("/invoices/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMERS_INVOICES", "USER")

                                                // === Gestión de usuarios: reglas específicas (antes de la general) ===
                                                // DELETE /users/{id} — eliminación física: solo SUPER_ADMIN
                                                .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("SUPER_ADMIN")
                                                // PUT /users/{id}/role y PUT /users/{id}/role/{roleId} — solo
                                                // SUPER_ADMIN
                                                .requestMatchers(HttpMethod.PUT, "/users/*/role").hasRole("SUPER_ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/users/*/role/*")
                                                .hasRole("SUPER_ADMIN")
                                                // DELETE /users/{id}/role — revocar rol: solo SUPER_ADMIN
                                                .requestMatchers(HttpMethod.DELETE, "/users/*/role")
                                                .hasRole("SUPER_ADMIN")
                                                // DELETE /users/me — autoelimación: cualquier usuario autenticado
                                                .requestMatchers(HttpMethod.DELETE, "/users/me").authenticated()
                                                // Resto de operaciones /users/** — ADMIN y superiores
                                                .requestMatchers("/users/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMERS_INVOICES", "SUPPORT")

                                                // === Wishlist: operaciones del usuario autenticado ===
                                                .requestMatchers("/wishlist/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "USER")

                                                // === Cupones: validación pública ===
                                                .requestMatchers(HttpMethod.GET, "/coupons/*/validate")
                                                .permitAll()

                                                // === Cupones: CRUD solo ADMIN ===
                                                .requestMatchers("/coupons/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                // === Dashboard: métricas por rol (Issue #115) ===
                                                // Dashboard de pedidos — rol ORDERS
                                                .requestMatchers("/dashboard/orders/**")
                                                .hasRole("ORDERS")
                                                // Dashboard de catálogo — rol STORE
                                                .requestMatchers("/dashboard/store/**")
                                                .hasRole("STORE")
                                                // Dashboard de clientes — rol CUSTOMERS_INVOICES
                                                .requestMatchers("/dashboard/customers/**")
                                                .hasRole("CUSTOMERS_INVOICES")
                                                // Dashboard de soporte — rol SUPPORT
                                                .requestMatchers("/dashboard/support/**")
                                                .hasRole("SUPPORT")
                                                // Dashboard general — SUPER_ADMIN y ADMIN
                                                .requestMatchers("/dashboard/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                // === Inventario: lectura y gestión solo ADMIN ===
                                                // GET /inventory/** — consultar inventario y movimientos
                                                .requestMatchers(HttpMethod.GET, "/inventory/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")
                                                // PUT /inventory/** — actualizar metadatos del inventario
                                                .requestMatchers(HttpMethod.PUT, "/inventory/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")
                                                // POST /inventory/** — registrar movimientos de stock
                                                .requestMatchers(HttpMethod.POST, "/inventory/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                // === Resto de endpoints: solo administradores ===
                                                .anyRequest().hasAnyRole("SUPER_ADMIN", "ADMIN"))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(verifiedUserFilter, JwtFilter.class)
                                .build();
        }
}
