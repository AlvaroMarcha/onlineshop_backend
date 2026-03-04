package es.marcha.backend.dto.response.invoice;

import java.math.BigDecimal;
import java.util.List;

import es.marcha.backend.core.shared.domain.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class InvoiceDataDTO {

    private Long internalId;
    private Long orderId;
    private String invoiceNumber;
    private String issueDate;
    private String operationDate;
    private String dueDate;
    private String paymentMethod;
    private String notes;
    /** Suma de bases imponibles brutas (sin descuento). */
    private BigDecimal totalBase;
    /** IVA calculado sobre la base con descuento aplicado. */
    private BigDecimal totalTax;
    /** Base imponible neta = totalBase - discountAmount. */
    private BigDecimal discountedBase;
    /** Total a pagar = discountedBase + totalTax. */
    private BigDecimal totalAmount;
    /** Importe del descuento sobre la base (null si no hay cupón). */
    private BigDecimal discountAmount;
    /** Código del cupón aplicado (null si no hay cupón). */
    private String couponCode;
    /** Tipo de descuento: PERCENTAGE o FIXED (null si no hay cupón). */
    private DiscountType discountType;
    /**
     * Valor nominal del cupón: porcentaje o importe fijo (null si no hay cupón).
     */
    private BigDecimal discountValue;
    private InvoiceCustomerDTO customer;
    private List<InvoiceLineDTO> lines;
    /** Resumen de IVA calculado sobre la base con descuento. */
    private List<TaxSummaryDTO> taxSummary;
}
