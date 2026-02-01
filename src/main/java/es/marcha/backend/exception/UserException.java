package es.marcha.backend.exception;

public class UserException extends NoHandlerException {

    public static final String DEFAULT = "USER_NOT_FOUND";
    public static final String FAILED_FETCH = "FAILED_TO_FETCH_USERS";
    public static final String FAILED_SAVE = "FAILED_TO_SAVE_USER";
    public static final String FAILED_UPDATE = "FAILED_TO_UPDATE_USER";
    public static final String FAILED_DELETE = "FAILED_TO_DELETE_USER";

    public UserException() {
        this(DEFAULT);
    }

    public UserException(String msg) {
        super(msg);
    }

    public UserException(String msg, Throwable cause) {
        super(msg);
    }

}
