package es.marcha.backend.controller.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import es.marcha.backend.exception.AddressException;
import es.marcha.backend.exception.InvoiceException;
import es.marcha.backend.exception.MediaException;
import es.marcha.backend.exception.NoHandlerException;
import es.marcha.backend.exception.OrderException;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.exception.RateLimitException;
import es.marcha.backend.exception.UserException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de tipo {@link RateLimitException} (429 Too Many
     * Requests).
     * <p>
     * Añade el header {@code Retry-After} con los segundos que el cliente debe
     * esperar
     * antes de reintentar la petición.
     * </p>
     *
     * @param ex La excepción de rate limit capturada.
     * @return Un {@link ResponseEntity} con código 429, header Retry-After y cuerpo
     *         JSON.
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        return new ResponseEntity<>(error, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Maneja las excepciones de tipo {@link NoHandlerException} lanzadas por los
     * controllers.
     * <p>
     * Este método captura la excepción y construye un objeto {@link ErrorResponse}
     * que se devuelve
     * como cuerpo de la respuesta HTTP. Además, mapea el mensaje de la excepción a
     * un código HTTP
     * adecuado.
     * </p>
     *
     * <p>
     * La lógica de mapeo de estado HTTP es la siguiente:
     * <ul>
     * <li>{@link UserException#DEFAULT} → 404 Not Found</li>
     * <li>{@link UserException#FAILED_SAVE}, {@link UserException#FAILED_UPDATE} →
     * 400 Bad
     * Request</li>
     * <li>Cualquier otro mensaje → 500 Internal Server Error</li>
     * </ul>
     * </p>
     *
     * @param ex La excepción de tipo {@link NoHandlerException} capturada.
     * @return Un {@link ResponseEntity} que contiene el {@link ErrorResponse} y el
     *         código de estado
     *         HTTP correspondiente.
     */
    @ExceptionHandler(NoHandlerException.class)
    public ResponseEntity<ErrorResponse> handlerException(NoHandlerException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());

        HttpStatus status = switch (ex.getMessage()) {
            case UserException.DEFAULT,
                    AddressException.DEFAULT,
                    AddressException.ADDRESSES_NOT_FOUND ->
                HttpStatus.NOT_FOUND;
            case UserException.FAILED_SAVE,
                    AddressException.FAILED_SAVE ->
                HttpStatus.BAD_REQUEST;
            case UserException.FAILED_UPDATE,
                    AddressException.FAILED_UPDATE ->
                HttpStatus.BAD_REQUEST;
            case UserException.FAILED_DELETE,
                    AddressException.FAILED_DELETE ->
                HttpStatus.BAD_REQUEST;
            case UserException.FAILED_FETCH -> HttpStatus.INTERNAL_SERVER_ERROR;
            case UserException.FAILED_LOGIN -> HttpStatus.UNAUTHORIZED;
            case UserException.FAILED_REGISTER -> HttpStatus.BAD_REQUEST;
            case UserException.FAILED_CREATE_USER -> HttpStatus.CONFLICT;
            case UserException.TOKEN_FAILED -> HttpStatus.UNAUTHORIZED;
            case UserException.USER_LOGGEDOUT -> HttpStatus.FORBIDDEN;
            case UserException.INVALID_RESET_TOKEN -> HttpStatus.BAD_REQUEST;
            case UserException.RESET_TOKEN_EXPIRED -> HttpStatus.GONE;
            case UserException.TERMS_NOT_ACCEPTED -> HttpStatus.BAD_REQUEST;
            case InvoiceException.DEFAULT, InvoiceException.ADDRESS_NOT_FOUND ->
                HttpStatus.NOT_FOUND;
            case InvoiceException.ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case InvoiceException.PDF_FAILED,
                    InvoiceException.STORAGE_ERROR ->
                HttpStatus.INTERNAL_SERVER_ERROR;
            case InvoiceException.FAILED_FETCH -> HttpStatus.NOT_FOUND;
            case OrderException.INVALID_STATUS_TRANSITION -> HttpStatus.CONFLICT;
            case ProductException.INSUFFICIENT_STOCK -> HttpStatus.CONFLICT;
            case MediaException.INVALID_FILE_TYPE,
                    MediaException.INVALID_FILE_CONTENT,
                    MediaException.FILE_TOO_LARGE ->
                HttpStatus.BAD_REQUEST;
            case MediaException.DEFAULT_MESSAGE -> HttpStatus.BAD_REQUEST;
            case MediaException.STORAGE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(error, status);
    }
}
