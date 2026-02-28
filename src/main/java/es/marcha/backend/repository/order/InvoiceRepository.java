package es.marcha.backend.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.order.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Todas las facturas pertenecientes a un usuario concreto. */
    List<Invoice> findAllByUserId(long userId);

    /** Factura generada para un pedido dado (como máximo una). */
    Optional<Invoice> findByOrderId(long orderId);

    /** Busca una factura por su número legible. */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
