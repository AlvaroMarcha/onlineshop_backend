package es.marcha.backend.core.error.exception;

public class WishlistException extends NoHandlerException {

    public static final String DEFAULT = "WISHLIST_NOT_FOUND";
    public static final String ITEM_NOT_FOUND = "WISHLIST_ITEM_NOT_FOUND";
    public static final String ITEM_ALREADY_EXISTS = "WISHLIST_ITEM_ALREADY_EXISTS";
    public static final String PRODUCT_NOT_FOUND = "WISHLIST_PRODUCT_NOT_FOUND";

    public WishlistException() {
        this(DEFAULT);
    }

    public WishlistException(String message) {
        super(message);
    }

    public WishlistException(String message, Throwable cause) {
        super(message, cause);
    }
}
