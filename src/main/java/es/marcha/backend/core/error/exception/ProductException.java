package es.marcha.backend.core.error.exception;

public class ProductException extends NoHandlerException {

    public static final String DEFAULT = "PRODUCT_NOT_FOUND";
    public static final String FAILED_FETCH = "FAILED_TO_FETCH_PRODUCTS";
    public static final String FAILED_CREATE = "FAILED_TO_CREATE_PRODUCT";
    public static final String FAILED_UPDATE = "FAILED_TO_UPDATE_PRODUCT";
    public static final String FAILED_DELETE = "FAILED_TO_DELETE_PRODUCT";
    public static final String FAILED_FETCH_SUBCATEGORY = "FAILED_TO_FETCH_SUBCATEGORY";
    public static final String FAILED_CREATE_SUBCATEGORY = "FAILED_TO_CREATE_SUBCATEGORY";
    public static final String FAILED_UPDATE_SUBCATEGORY = "FAILED_TO_UPDATE_SUBCATEGORY";
    public static final String FAILED_DELETE_SUBCATEGORY = "FAILED_TO_DELETE_SUBCATEGORY";
    public static final String FAILED_FETCH_CATEGORY = "FAILED_TO_FETCH_CATEGORY";
    public static final String FAILED_SAVE_CATEGORY = "FAILED_TO_SAVE_CATEGORY";
    public static final String FAILED_DELETE_CATEGORY = "FAILED_TO_DELETE_CATEGORY";
    public static final String FAILED_UPDATE_CATEGORY = "FAILED_TO_UPDATE_CATEGORY";
    public static final String FAILED_FETCH_REVIEWS = "FAILED_TO_FETCH_REVIEWS";
    public static final String FAILED_FETCH_REVIEW = "FAILED_TO_FETCH_REVIEW";
    public static final String FAILED_CREATE_REVIEW = "FAILED_TO_CREATE_REVIEW";
    public static final String FAILED_SAVE_REVIEW = "FAILED_TO_SAVE_REVIEW";
    public static final String FAILED_UPDATE_REVIEW = "FAILED_TO_UPDATE_REVIEW";
    public static final String NOT_VALID_RATING = "RATING_MUST_BE_BETWEEN_1_AND_5";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String STOCK_UPDATED = "STOCK_UPDATED";
    public static final String INVALID_INITIAL_STOCK = "INVALID_INITIAL_STOCK_VALUE";

    public ProductException() {
        this(DEFAULT);
    }

    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }

}
