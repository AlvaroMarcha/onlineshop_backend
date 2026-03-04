package es.marcha.backend.core.config;

import java.util.Map;
import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bean que guarda el estado de habilitación de cada módulo de negocio.
 *
 * <p>
 * Las propiedades se pueden establecer en <code>application.properties</code>
 * como <code>modules.cart=true</code>, <code>modules.invoice=false</code>, etc.
 *
 * <p>
 * Los valores tienen por defecto <code>false</code> para que todos los módulos
 * arranquen deshabilitados. Pueden cambiarse dinámicamente llamando a los
 * setters
 * (por ejemplo desde un controlador de administración).
 */
@Component
@ConfigurationProperties(prefix = "modules")
public class ModuleProperties {

    private boolean cart = false;
    private boolean catalog = false;
    private boolean company = false;
    private boolean coupon = false;
    private boolean invoice = false;
    private boolean notification = false;
    private boolean order = false;
    private boolean wishlist = false;

    // getters & setters for each field

    public boolean isCart() {
        return cart;
    }

    public void setCart(boolean cart) {
        this.cart = cart;
    }

    public boolean isCatalog() {
        return catalog;
    }

    public void setCatalog(boolean catalog) {
        this.catalog = catalog;
    }

    public boolean isCompany() {
        return company;
    }

    public void setCompany(boolean company) {
        this.company = company;
    }

    public boolean isCoupon() {
        return coupon;
    }

    public void setCoupon(boolean coupon) {
        this.coupon = coupon;
    }

    public boolean isInvoice() {
        return invoice;
    }

    public void setInvoice(boolean invoice) {
        this.invoice = invoice;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }

    public boolean isWishlist() {
        return wishlist;
    }

    public void setWishlist(boolean wishlist) {
        this.wishlist = wishlist;
    }

    /**
     * Consulta genérica por nombre de módulo.
     */
    public boolean isEnabled(String module) {
        return switch (module) {
            case "cart" -> cart;
            case "catalog" -> catalog;
            case "company" -> company;
            case "coupon" -> coupon;
            case "invoice" -> invoice;
            case "notification" -> notification;
            case "order" -> order;
            case "wishlist" -> wishlist;
            default -> false;
        };
    }

    public void setEnabled(String module, boolean value) {
        switch (module) {
            case "cart" -> setCart(value);
            case "catalog" -> setCatalog(value);
            case "company" -> setCompany(value);
            case "coupon" -> setCoupon(value);
            case "invoice" -> setInvoice(value);
            case "notification" -> setNotification(value);
            case "order" -> setOrder(value);
            case "wishlist" -> setWishlist(value);
            default -> {
                /* ignore */ }
        }
    }

    /**
     * Devuelve un mapa con los valores actuales (uso para administración).
     */
    public Map<String, Boolean> asMap() {
        Map<String, Boolean> m = new HashMap<>();
        m.put("cart", cart);
        m.put("catalog", catalog);
        m.put("company", company);
        m.put("coupon", coupon);
        m.put("invoice", invoice);
        m.put("notification", notification);
        m.put("order", order);
        m.put("wishlist", wishlist);
        return m;
    }
}
