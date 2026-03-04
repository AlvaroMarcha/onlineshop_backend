package es.marcha.backend.modules.invoice.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.invoice.domain.model.Invoice;
import jakarta.persistence.LockModeType;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Todas las facturas pertenecientes a un usuario concreto. */
    List<Invoice> findAllByUserId(long userId);

    /** Factura generada para un pedido dado (como máximo una). */
    Optional<Invoice> findByOrderId(long orderId);

    /** Busca una factura por su número legible. */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Devuelve la última factura emitida en el año indicado bloqueando la fila
     * para evitar números duplicados ante peticiones concurrentes.
     * El prefijo debe tener el formato {@code INV-YYYY-}.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT(:prefix, '%') ORDER BY i.invoiceNumber DESC")
    List<Invoice> findLastByYearPrefix(@Param("prefix") String prefix);
}
