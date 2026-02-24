package es.marcha.backend.exception;

public class OrderException extends NoHandlerException {

    public static final String DEFAULT = "ORDER_NOT_FOUND";
    public static final String FAILED_FETCH = "FAILED_TO_FETCH_ORDERS";
    public static final String NOT_VALID_PAYMENT = "NOT_VALID_PAYMENT FOUNDS";
    public static final String TERMINAL_STATUS_PAYMENT = "PAYMENT_HAS_A_FINAL_STATUS";
    public static final String DUPLICATE_TRANSACTION = "TRANSACTION_ID_DUPLICATED";
    public static final String FAILED_ORDER_ADDRESSES = "FAILED_TO_FETCH_ORDER_ADDRESS";
    public static final String FAILED_ORDER_ADDRESS = "ORDER_ADDRESS_NOT_FOUND";
    public static final String USER_ADDRESS_LENGHT_0 = "USER_ADDRESS_CANNOT_BE_0";
    public static final String FAILED_ORDER_ITEMS = "FAILED_TO_FETCH_ORDER_ITEMS";

    public OrderException() {
        this(DEFAULT);
    }

    public OrderException(String msg) {
        super(msg);
    }

}
