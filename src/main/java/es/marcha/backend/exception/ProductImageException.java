package es.marcha.backend.exception;

public class ProductImageException extends NoHandlerException {

    public static final String NOT_FOUND = "PRODUCT_IMAGE_NOT_FOUND";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String FAILED_UPLOAD = "PRODUCT_IMAGE_UPLOAD_FAILED";
    public static final String FAILED_DELETE = "PRODUCT_IMAGE_DELETE_FAILED";
    public static final String FAILED_UPDATE = "PRODUCT_IMAGE_UPDATE_FAILED";
    public static final String MISMATCH = "PRODUCT_IMAGE_DOES_NOT_BELONG_TO_PRODUCT";
    public static final String MAX_IMAGES_EXCEEDED = "PRODUCT_IMAGE_MAX_LIMIT_EXCEEDED";

    public ProductImageException() {
        this(NOT_FOUND);
    }

    public ProductImageException(String message) {
        super(message);
    }

    public ProductImageException(String message, Throwable cause) {
        super(message, cause);
    }
}
