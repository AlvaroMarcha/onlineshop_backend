package es.marcha.backend.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro que bloquea operaciones sensibles para usuarios cuyo email no ha sido
 * verificado.
 * <p>
 * Se ejecuta después de {@link JwtFilter}. Si la ruta entra en la lista de
 * rutas
 * protegidas por verificación y el usuario tiene {@code isVerified = false},
 * devuelve HTTP 403 con el código {@code USER_NOT_VERIFIED}.
 * </p>
 * <p>
 * Rutas protegidas (requieren verified = true):
 * <ul>
 * <li>POST/PUT/DELETE {@code /orders/**} — crear y modificar pedidos</li>
 * <li>POST/PUT/DELETE {@code /cart/**} — gestionar carrito</li>
 * <li>POST {@code /stripe/**} — iniciar pagos</li>
 * <li>POST/PUT/DELETE {@code /address/**} — gestionar direcciones</li>
 * <li>POST/PUT/DELETE {@code /invoices/**} — crear facturas</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class VerifiedUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /** Prefijos de ruta que requieren que el usuario esté verificado. */
    private static final Set<String> VERIFIED_REQUIRED_PREFIXES = Set.of(
            "/orders",
            "/cart",
            "/stripe",
            "/address",
            "/invoices");

    /** Métodos HTTP de escritura que deben comprobar la verificación. */
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Solo comprobar si la ruta y el método requieren verificación
        if (!requiresVerification(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener usuario autenticado del SecurityContextHolder (establecido por
        // JwtFilter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Consultar verificación en BD
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null && !user.isVerified()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(new ErrorBody(UserException.USER_NOT_VERIFIED)));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Determina si la combinación método + ruta requiere verificación de email. */
    private boolean requiresVerification(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        if (!WRITE_METHODS.contains(method)) {
            return false;
        }
        String path = request.getServletPath();
        return VERIFIED_REQUIRED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    /** Cuerpo JSON mínimo para la respuesta de error. */
    private record ErrorBody(String message) {
    }
}
