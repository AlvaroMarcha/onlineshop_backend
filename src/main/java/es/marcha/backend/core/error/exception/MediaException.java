package es.marcha.backend.core.error.exception;

public class MediaException extends NoHandlerException {

    public static final String DEFAULT_MESSAGE = "EMPTY_FILE";
    public static final String INVALID_FILE_TYPE = "INVALID_FILE_TYPE";
    public static final String INVALID_FILE_CONTENT = "INVALID_FILE_CONTENT";
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String STORAGE_ERROR = "STORAGE_ERROR";

    public MediaException() {
        super(DEFAULT_MESSAGE);
    }

    public MediaException(String message) {
        super(message);
    }

    public MediaException(String message, Throwable cause) {
        super(message, cause);
    }

}
