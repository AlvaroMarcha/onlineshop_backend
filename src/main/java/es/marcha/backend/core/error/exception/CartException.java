package es.marcha.backend.core.error.exception;

public class CartException extends NoHandlerException {

    public static final String DEFAULT = "CART_NOT_FOUND";
    public static final String ITEM_NOT_FOUND = "CART_ITEM_NOT_FOUND";
    public static final String EMPTY_CART = "CART_IS_EMPTY";
    public static final String QUANTITY_INVALID = "CART_ITEM_QUANTITY_INVALID";

    public CartException() {
        this(DEFAULT);
    }

    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
