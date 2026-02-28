package es.marcha.backend.dto.response.invoice;

import java.math.BigDecimal;
import java.util.List;

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

    private String invoiceNumber;
    private String issueDate;
    private String operationDate;
    private String dueDate;
    private String paymentMethod;
    private String notes;
    private BigDecimal totalBase;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private InvoiceCustomerDTO customer;
    private List<InvoiceLineDTO> lines;
    private List<TaxSummaryDTO> taxSummary;
}
