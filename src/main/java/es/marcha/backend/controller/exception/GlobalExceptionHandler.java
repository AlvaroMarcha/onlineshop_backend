package es.marcha.backend.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import es.marcha.backend.exception.AddressException;
import es.marcha.backend.exception.NoHandlerException;
import es.marcha.backend.exception.UserException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(error, status);
    }
}
