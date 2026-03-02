package es.marcha.backend.services.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.user.DataExportResponseDTO;
import es.marcha.backend.dto.response.user.DataExportResponseDTO.AddressExport;
import es.marcha.backend.dto.response.user.DataExportResponseDTO.InvoiceExport;
import es.marcha.backend.dto.response.user.DataExportResponseDTO.OrderExport;
import es.marcha.backend.dto.response.user.DataExportResponseDTO.PaymentExport;
import es.marcha.backend.dto.response.user.DataExportResponseDTO.ProfileExport;
import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.order.Invoice;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.Payment;
import es.marcha.backend.model.user.Address;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.order.InvoiceRepository;
import es.marcha.backend.repository.order.OrderRepository;
import es.marcha.backend.repository.order.PaymentRepository;
import es.marcha.backend.repository.user.AddressRepository;
import es.marcha.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataExportService {

    private final UserRepository uRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Genera la exportación completa de los datos personales del usuario
     * autenticado
     * conforme al Art. 20 del RGPD (portabilidad de datos).
     * <p>
     * La respuesta incluye perfil, direcciones, pedidos, facturas y pagos del
     * usuario.
     * Los datos de pedidos se conservan aunque el usuario esté anonimizado.
     *
     * @param username el username del usuario autenticado extraído del JWT
     * @return {@link DataExportResponseDTO} con todos los datos del usuario
     * @throws UserException si el usuario no existe en el sistema
     */
    public DataExportResponseDTO export(String username) {
        User user = uRepository.findByUsername(username)
                .orElseThrow(() -> new UserException());

        List<Address> addresses = addressRepository.findAllByUserId(user.getId());
        List<Order> orders = orderRepository.findAllByUserId(user.getId());
        List<Invoice> invoices = invoiceRepository.findAllByUserId(user.getId());
        List<Payment> payments = orders.stream()
                .flatMap(o -> paymentRepository.findAllByOrderId(o.getId()).stream())
                .collect(Collectors.toList());

        log.info("Exportación de datos generada para usuario id: {}", user.getId());

        return DataExportResponseDTO.builder()
                .exportedAt(LocalDateTime.now().format(ISO_FMT))
                .profile(mapProfile(user))
                .addresses(addresses.stream().map(this::mapAddress).collect(Collectors.toList()))
                .orders(orders.stream().map(this::mapOrder).collect(Collectors.toList()))
                .invoices(invoices.stream().map(this::mapInvoice).collect(Collectors.toList()))
                .payments(payments.stream().map(this::mapPayment).collect(Collectors.toList()))
                .build();
    }

    private ProfileExport mapProfile(User user) {
        return ProfileExport.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .active(user.isActive())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(ISO_FMT) : null)
                .termsVersion(user.getTermsVersion())
                .termsAcceptedAt(user.getTermsAcceptedAt() != null ? user.getTermsAcceptedAt().format(ISO_FMT) : null)
                .build();
    }

    private AddressExport mapAddress(Address a) {
        return AddressExport.builder()
                .id(a.getId())
                .type(a.getType() != null ? a.getType().toString() : null)
                .isDefault(a.isDefault())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .country(a.getCountry())
                .postalCode(a.getPostalCode())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().format(ISO_FMT) : null)
                .updatedAt(a.getUpdatedAt() != null ? a.getUpdatedAt().format(ISO_FMT) : null)
                .build();
    }

    private OrderExport mapOrder(Order o) {
        return OrderExport.builder()
                .id(o.getId())
                .status(o.getStatus() != null ? o.getStatus().toString() : null)
                .paymentMethod(o.getPaymentMethod())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().format(ISO_FMT) : null)
                .build();
    }

    private InvoiceExport mapInvoice(Invoice inv) {
        return InvoiceExport.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .status(inv.getStatus() != null ? inv.getStatus().toString() : null)
                .issueDate(inv.getIssueDate() != null ? inv.getIssueDate().toString() : null)
                .totalAmount(inv.getTotalAmount())
                .build();
    }

    private PaymentExport mapPayment(Payment p) {
        return PaymentExport.builder()
                .id(p.getId())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .status(p.getStatus() != null ? p.getStatus().toString() : null)
                .amount(p.getAmount())
                .provider(p.getProvider())
                .transactionId(p.getTransactionId())
                .build();
    }
}
