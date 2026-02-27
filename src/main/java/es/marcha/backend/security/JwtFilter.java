package es.marcha.backend.security;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtFilter extends OncePerRequestFilter {

    /**
     * Intercepta cada petición HTTP y valida el token JWT presente en la cabecera {@code Authorization}.
     * <p>
     * Si la ruta es pública ({@code /auth/login} o {@code /auth/register}), se omite la validación.
     * Si el token es válido, se construye un objeto de autenticación y se registra en el
     * {@link SecurityContextHolder} para que Spring Security lo trate como usuario autenticado.
     * Si el token es inválido o ha expirado, se devuelve HTTP 401 Unauthorized.
     * </p>
     *
     * @param request     La petición HTTP entrante.
     * @param response    La respuesta HTTP.
     * @param filterChain La cadena de filtros que continúa el procesamiento de la petición.
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
                // validar token y sacar usuario
                String user = JwtUtil.validateToken(token);

                // crear Authentication sin roles
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, null);

                // meterlo en el contexto de Spring
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN_OR_EXPIRED");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
