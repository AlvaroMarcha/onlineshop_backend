package es.marcha.backend.core.error.exception;

public class AddressException extends NoHandlerException {
    // Statics outputs
    public static final String DEFAULT = "ADDRESS_NOT_FOUND";
    public static final String ADDRESSES_NOT_FOUND = "ADDRESSES_NOT_FOUND";
    public static final String FAILED_SAVE = "FAILED_TO_SAVE_ADDRESS";
    public static final String FAILED_UPDATE = "FAILED_TO_UPDATE_ADDRESS";
    public static final String FAILED_DELETE = "FAILED_TO_DELETE_ADDRESS";

    public AddressException() {
        this(DEFAULT);
    }

    public AddressException(String msg) {
        super(msg);
    }

    public AddressException(String msg, Throwable cause) {
        super(msg);
    }

}
