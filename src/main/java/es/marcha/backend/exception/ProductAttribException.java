package es.marcha.backend.exception;

public class ProductAttribException extends NoHandlerException {

    // Excepciones para ProductAttrib
    public static final String ATTRIB_NOT_FOUND = "ATTRIB_NOT_FOUND";
    public static final String FAILED_FETCH_ATTRIB = "FAILED_TO_FETCH_ATTRIB";
    public static final String FAILED_FETCH_ATTRIBS = "FAILED_TO_FETCH_ATTRIBS";
    public static final String FAILED_CREATE_ATTRIB = "FAILED_TO_CREATE_ATTRIB";
    public static final String FAILED_UPDATE_ATTRIB = "FAILED_TO_UPDATE_ATTRIB";
    public static final String FAILED_DELETE_ATTRIB = "FAILED_TO_DELETE_ATTRIB";
    public static final String SLUG_ALREADY_EXISTS = "ATTRIB_SLUG_ALREADY_EXISTS";

    // Excepciones para ProductAttribValue
    public static final String ATTRIB_VALUE_NOT_FOUND = "ATTRIB_VALUE_NOT_FOUND";
    public static final String FAILED_FETCH_ATTRIB_VALUES = "FAILED_TO_FETCH_ATTRIB_VALUES";
    public static final String FAILED_CREATE_ATTRIB_VALUE = "FAILED_TO_CREATE_ATTRIB_VALUE";
    public static final String FAILED_UPDATE_ATTRIB_VALUE = "FAILED_TO_UPDATE_ATTRIB_VALUE";
    public static final String FAILED_DELETE_ATTRIB_VALUE = "FAILED_TO_DELETE_ATTRIB_VALUE";

    // Excepciones para ProductVariant
    public static final String VARIANT_NOT_FOUND = "VARIANT_NOT_FOUND";
    public static final String FAILED_FETCH_VARIANT = "FAILED_TO_FETCH_VARIANT";
    public static final String FAILED_FETCH_VARIANTS = "FAILED_TO_FETCH_VARIANTS";
    public static final String FAILED_CREATE_VARIANT = "FAILED_TO_CREATE_VARIANT";
    public static final String FAILED_UPDATE_VARIANT = "FAILED_TO_UPDATE_VARIANT";
    public static final String FAILED_DELETE_VARIANT = "FAILED_TO_DELETE_VARIANT";
    public static final String DUPLICATE_ATTRIB_IN_VARIANT = "VARIANT_ALREADY_HAS_VALUE_FOR_THIS_ATTRIB";
    public static final String ATTRIB_VALUE_NOT_BELONGS_TO_PRODUCT = "ATTRIB_VALUE_DOES_NOT_BELONG_TO_PRODUCT_ATTRIBS";
    public static final String SKU_ALREADY_EXISTS = "VARIANT_SKU_ALREADY_EXISTS";

    public ProductAttribException() {
        this(ATTRIB_NOT_FOUND);
    }

    public ProductAttribException(String message) {
        super(message);
    }

    public ProductAttribException(String message, Throwable cause) {
        super(message, cause);
    }
}
