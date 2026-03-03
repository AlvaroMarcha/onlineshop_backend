package es.marcha.backend.security;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.marcha.backend.services.security.TokenInvalidationService;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private TokenInvalidationService tokenInvalidationService;

    /**
     * Intercepta cada petición HTTP y valida el token JWT presente en la cabecera
     * {@code Authorization}.
     * <p>
     * Si la ruta es pública ({@code /auth/login} o {@code /auth/register}), se
     * omite la validación.
     * Si el token es válido, extrae el username y el rol, construye el objeto de
     * autenticación
     * con {@link SimpleGrantedAuthority} y lo registra en el
     * {@link SecurityContextHolder}.
     * Si el token es inválido o ha expirado, devuelve HTTP 401 Unauthorized.
     * </p>
     *
     * @param request     La petición HTTP entrante.
     * @param response    La respuesta HTTP.
     * @param filterChain La cadena de filtros que continúa el procesamiento de la
     *                    petición.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        // Ignorar rutas públicas
        if (path.startsWith("/auth/login") || path.startsWith("/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = JwtUtil.validateToken(token);
                String roleName = JwtUtil.getRoleFromToken(token);

                // Rechazar el token si el usuario tiene un JWT invalidado (p. ej. por cambio de
                // rol)
                if (tokenInvalidationService.isInvalidated(username)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_INVALIDATED_ROLE_CHANGED");
                    return;
                }

                List<SimpleGrantedAuthority> authorities = (roleName != null && !roleName.isBlank())
                        ? List.of(new SimpleGrantedAuthority(roleName))
                        : List.of();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
                        null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN_OR_EXPIRED");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
