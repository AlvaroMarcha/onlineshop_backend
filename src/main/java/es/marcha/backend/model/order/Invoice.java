package es.marcha.backend.model.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import es.marcha.backend.model.enums.InvoiceStatus;
import es.marcha.backend.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "invoice_number", nullable = false, unique = true, length = 32)
    private String invoiceNumber;
    @Column(name = "pdf_path")
    private String pdfPath;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
