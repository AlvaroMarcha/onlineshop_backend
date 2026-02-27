package es.marcha.backend.exception;

public class RolePermissionsException extends NoHandlerException {

    public static final String DEFAULT = "NOT_ALLOWED";
    public static final String NOT_EXIST = "ROLE_NOT_EXIST";
    public static final String FAILED_FETCH = "FAILED_TO_FETCH_ROLES";
    public static final String ROLE_ALREADY_EXIST = "ROLE_ALREADY_EXIST";

    public RolePermissionsException() {
        this(DEFAULT);
    }

    public RolePermissionsException(String msg) {
        super(msg);
    }
}
