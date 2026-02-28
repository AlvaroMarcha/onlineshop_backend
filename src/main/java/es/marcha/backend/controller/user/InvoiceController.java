package es.marcha.backend.controller.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.model.order.Invoice;
import es.marcha.backend.services.invoice.InvoiceService;

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
     * @return la entidad {@link Invoice} con HTTP 201 Created.
     */
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable long orderId) {
        Invoice invoice = invoiceService.generateInvoice(orderId);
        return new ResponseEntity<>(invoice, HttpStatus.CREATED);
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
     * Recupera una factura por su número legible (p. ej. {@code INV-2026-000042}).
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
