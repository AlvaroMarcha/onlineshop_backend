package es.marcha.backend.core.error.exception;

public class CouponException extends NoHandlerException {

    public static final String DEFAULT = "COUPON_NOT_FOUND";
    public static final String INACTIVE = "COUPON_INACTIVE";
    public static final String EXPIRED = "COUPON_EXPIRED";
    public static final String NOT_YET_VALID = "COUPON_NOT_YET_VALID";
    public static final String MAX_USES_REACHED = "COUPON_MAX_USES_REACHED";
    public static final String MIN_AMOUNT_NOT_MET = "COUPON_MIN_ORDER_AMOUNT_NOT_MET";
    public static final String NOT_APPLICABLE_TO_USER = "COUPON_NOT_APPLICABLE_TO_USER";
    public static final String CODE_ALREADY_EXISTS = "COUPON_CODE_ALREADY_EXISTS";

    public CouponException() {
        this(DEFAULT);
    }

    public CouponException(String message) {
        super(message);
    }

    public CouponException(String message, Throwable cause) {
        super(message, cause);
    }
}
