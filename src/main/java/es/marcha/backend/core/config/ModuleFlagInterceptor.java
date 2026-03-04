package es.marcha.backend.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que comprueba, antes de ejecutar cualquier controlador, si el
 * módulo asociado a la ruta actual está habilitado.
 *
 * <p>
 * Se basa en la primera parte de la URI (/cart, /products, /orders, etc.) y
 * lo mapea a un "nombre de módulo" concreto. Si el módulo está desactivado,
 * responde con 404 y evita la ejecución del handler.
 */
@Component
public class ModuleFlagInterceptor implements HandlerInterceptor {

    private final ModuleProperties moduleProps;

    @Autowired
    public ModuleFlagInterceptor(ModuleProperties moduleProps) {
        this.moduleProps = moduleProps;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String module = extractModule(path);
        if (module != null && !moduleProps.isEnabled(module)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return false;
        }
        return true;
    }

    private String extractModule(String uri) {
        // uri empieza por '/'
        String[] segments = uri.split("/");
        if (segments.length < 2) {
            return null;
        }
        switch (segments[1]) {
            case "cart":
                return "cart";
            case "products":
            case "categories":
                return "catalog";
            case "company":
                return "company";
            case "coupons":
                return "coupon";
            case "invoices":
                return "invoice";
            case "orders":
            case "stripe":
                return "order";
            case "wishlist":
                return "wishlist";
            default:
                return null; // no es ruta de módulo
        }
    }
}
