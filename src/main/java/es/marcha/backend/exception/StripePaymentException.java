package es.marcha.backend.exception;

public class StripePaymentException extends NoHandlerException {

    public static final String DEFAULT = "STRIPE_PAYMENT_ERROR";
    public static final String FAILED_CREATE_INTENT = "STRIPE_FAILED_TO_CREATE_PAYMENT_INTENT";
    public static final String FAILED_RETRIEVE_INTENT = "STRIPE_FAILED_TO_RETRIEVE_PAYMENT_INTENT";
    public static final String INVALID_WEBHOOK_SIGNATURE = "STRIPE_INVALID_WEBHOOK_SIGNATURE";
    public static final String FAILED_DESERIALIZE_EVENT = "STRIPE_FAILED_TO_DESERIALIZE_EVENT";
    public static final String PAYMENT_NOT_FOUND = "STRIPE_PAYMENT_NOT_FOUND";
    public static final String FAILED_UPDATE_PAYMENT = "STRIPE_FAILED_TO_UPDATE_PAYMENT";

    public StripePaymentException() {
        this(DEFAULT);
    }

    public StripePaymentException(String msg) {
        super(msg);
    }
}
