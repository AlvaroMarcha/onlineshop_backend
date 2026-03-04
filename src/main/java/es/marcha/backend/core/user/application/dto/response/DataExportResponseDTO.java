package es.marcha.backend.core.user.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataExportResponseDTO {

    @Builder.Default
    private String schemaVersion = "1.0";
    private String exportedAt;

    private ProfileExport profile;
    private List<AddressExport> addresses;
    private List<OrderExport> orders;
    private List<InvoiceExport> invoices;
    private List<PaymentExport> payments;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileExport {
        private Long id;
        private String name;
        private String surname;
        private String username;
        private String email;
        private String phone;
        private String role;
        private boolean active;
        private String createdAt;
        private String termsVersion;
        private String termsAcceptedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressExport {
        private Long id;
        private String type;
        private boolean isDefault;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String country;
        private String postalCode;
        private String createdAt;
        private String updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderExport {
        private Long id;
        private String status;
        private String paymentMethod;
        private Double totalAmount;
        private String createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvoiceExport {
        private Long id;
        private String invoiceNumber;
        private String status;
        private String issueDate;
        private BigDecimal totalAmount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentExport {
        private Long id;
        private Long orderId;
        private String status;
        private Double amount;
        private String provider;
        private String transactionId;
    }
}
