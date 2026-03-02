package es.marcha.backend.exception;

public class RateLimitException extends NoHandlerException {

    public static final String TOO_MANY_REQUESTS = "TOO_MANY_REQUESTS";

    private final long retryAfterSeconds;

    public RateLimitException(long retryAfterSeconds) {
        super(TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
