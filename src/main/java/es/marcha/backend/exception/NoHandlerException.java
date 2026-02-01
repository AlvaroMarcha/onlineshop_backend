package es.marcha.backend.exception;

public abstract class NoHandlerException extends RuntimeException {

    protected NoHandlerException(String msg) {
        super(msg);
    }

    protected NoHandlerException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

