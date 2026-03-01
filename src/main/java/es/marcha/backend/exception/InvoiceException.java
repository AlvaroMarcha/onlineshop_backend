package es.marcha.backend.exception;

public class InvoiceException extends NoHandlerException {

    public static final String DEFAULT = "INVOICE_NOT_FOUND";
    public static final String ALREADY_EXISTS = "INVOICE_ALREADY_EXISTS";
    public static final String PDF_FAILED = "INVOICE_PDF_GENERATION_FAILED";
    public static final String STORAGE_ERROR = "INVOICE_STORAGE_ERROR";
    public static final String FAILED_FETCH = "INVOICE_FAILED_FETCH";
    public static final String ADDRESS_NOT_FOUND = "INVOICE_ORDER_ADDRESS_NOT_FOUND";

    public InvoiceException() {
        this(DEFAULT);
    }

    public InvoiceException(String msg) {
        super(msg);
    }

    public InvoiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
