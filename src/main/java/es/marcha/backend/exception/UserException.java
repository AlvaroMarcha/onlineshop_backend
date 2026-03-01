package es.marcha.backend.exception;

public class UserException extends NoHandlerException {

    public static final String DEFAULT = "USER_NOT_FOUND";
    public static final String FAILED_FETCH = "FAILED_TO_FETCH_USERS";
    public static final String FAILED_SAVE = "FAILED_TO_SAVE_USER";
    public static final String FAILED_UPDATE = "FAILED_TO_UPDATE_USER";
    public static final String FAILED_DELETE = "FAILED_TO_DELETE_USER";
    public static final String FAILED_LOGIN = "FAILED_TO_LOGIN";
    public static final String FAILED_REGISTER = "FAILED_TO_REGISTER";
    public static final String FAILED_CREATE_USER = "USER_ALREADY_EXIST";
    public static final String TOKEN_FAILED = "TOKEN_NOT_ALLOWED";
    public static final String USER_LOGGEDOUT = "USER_IS_LOGGEDOUT";
    public static final String INVALID_RESET_TOKEN = "INVALID_RESET_TOKEN";
    public static final String RESET_TOKEN_EXPIRED = "RESET_TOKEN_EXPIRED";

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
