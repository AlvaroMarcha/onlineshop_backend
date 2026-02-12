package es.marcha.backend.exception;

public class ProductException extends NoHandlerException {

    public static final String DEFAULT = "PRODUCT_NOT_FOUND";
    public static final String FAILED_FETCH = "FAILED_TO_FETCH_PRODUCTS";
    public static final String FAILED_CREATE = "FAILED_TO_CREATE_PRODUCT";
    public static final String FAILED_UPDATE = "FAILED_TO_UPDATE_PRODUCT";
    public static final String FAILED_DELETE = "FAILED_TO_DELETE_PRODUCT";
    public static final String FAILED_FETCH_SUBCATEGORY = "FAILED_TO_FETCH_SUBCATEGORY";
    public static final String FAILED_FETCH_CATEGORY = "FAILED_TO_FETCH_CATEGORY";
    public static final String FAILED_SAVE_CATEGORY = "FAILED_TO_SAVE_CATEGORY";
    public static final String FAILED_DELETE_CATEGORY = "FAILED_TO_DELETE_CATEGORY";
    public static final String FAILED_UPDATE_CATEGORY = "FAILED_TO_UPDATE_CATEGORY";

    public ProductException() {
        this(DEFAULT);
    }

    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable cause) {
        super(message);
    }

}
