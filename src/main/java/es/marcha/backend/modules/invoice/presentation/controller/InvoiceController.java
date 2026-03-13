package es.marcha.backend.modules.invoice.presentation.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.modules.invoice.domain.model.Invoice;
import es.marcha.backend.modules.invoice.application.service.InvoiceService;
import es.marcha.backend.modules.invoice.application.service.InvoiceService.InvoiceGenerationResult;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Genera un PDF de factura para el pedido indicado y lo persiste.
     * Si ya existe una factura para ese pedido, se devuelve la existente
     * (idempotente).
     *
     * @param orderId ID del pedido a facturar.
     * @return la entidad {@link Invoice} con HTTP 201 Created si es nueva,
     *         o HTTP 200 OK si la factura ya existía.
     */
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable long orderId) {
        InvoiceGenerationResult result = invoiceService.generateInvoice(orderId);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return new ResponseEntity<>(result.invoice(), status);
    }

    /**
     * Devuelve todas las facturas del usuario especificado.
     *
     * @param userId ID del usuario propietario.
     * @return lista de entidades {@link Invoice} con HTTP 200 OK.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Invoice>> getInvoicesByUser(@PathVariable long userId) {
        List<Invoice> invoices = invoiceService.getInvoicesByUser(userId);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    /**
     * Descarga o visualiza el PDF de la factura indicada.
     *
     * @param invoiceNumber número de referencia de la factura.
     * @param view          si {@code true} muestra el PDF en el navegador
     *                      (inline); si {@code false} o ausente lo descarga
     *                      (attachment).
     * @return el archivo PDF con HTTP 200 OK.
     */
    @GetMapping("/{invoiceNumber}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable String invoiceNumber,
            @RequestParam(defaultValue = "false") boolean view) {
        byte[] pdfBytes = invoiceService.getPdfBytes(invoiceNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                (view ? ContentDisposition.inline() : ContentDisposition.attachment())
                        .filename(invoiceNumber + ".pdf")
                        .build());
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * * Recupera una factura por su número legible (p. ej.
     * {@code INV-2026-000042}).
     *
     * @param invoiceNumber número de referencia de la factura.
     * @return la entidad {@link Invoice} con HTTP 200 OK.
     */
    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = invoiceService.getByInvoiceNumber(invoiceNumber);
        return new ResponseEntity<>(invoice, HttpStatus.OK);
    }
}
