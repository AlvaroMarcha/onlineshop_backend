package es.marcha.backend.services.invoice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import es.marcha.backend.config.CompanyProperties;
import es.marcha.backend.dto.response.invoice.CompanyDTO;
import es.marcha.backend.dto.response.invoice.InvoiceCustomerDTO;
import es.marcha.backend.dto.response.invoice.InvoiceDataDTO;
import es.marcha.backend.dto.response.invoice.InvoiceLineDTO;
import es.marcha.backend.dto.response.invoice.TaxSummaryDTO;
import es.marcha.backend.exception.InvoiceException;
import es.marcha.backend.model.enums.InvoiceStatus;
import es.marcha.backend.model.order.Invoice;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.OrderAddresses;
import es.marcha.backend.model.order.OrderItems;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.order.InvoiceRepository;
import es.marcha.backend.repository.order.OrderAddrRepository;
import es.marcha.backend.services.order.OrderService;
import jakarta.transaction.Transactional;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TEMPLATE_NAME = "invoices/invoice-default";
    private static final int PAYMENT_DAYS = 30;
    private static final int SCALE = 2;

    private final OrderService orderService;
    private final OrderAddrRepository orderAddrRepository;
    private final InvoiceRepository invoiceRepository;
    private final SpringTemplateEngine templateEngine;
    private final CompanyProperties companyProps;

    @Value("${app.invoices.storage-path}")
    private String storagePath;

    public InvoiceService(OrderService orderService,
            OrderAddrRepository orderAddrRepository,
            InvoiceRepository invoiceRepository,
            SpringTemplateEngine templateEngine,
            CompanyProperties companyProps) {
        this.orderService = orderService;
        this.orderAddrRepository = orderAddrRepository;
        this.invoiceRepository = invoiceRepository;
        this.templateEngine = templateEngine;
        this.companyProps = companyProps;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Genera, persiste y devuelve una factura para el pedido indicado.
     * Si ya existe una factura para ese pedido, se devuelve el registro existente
     * sin regenerar el PDF.
     *
     * @param orderId ID del pedido a facturar.
     * @return la entidad {@link Invoice} persistida.
     * @throws InvoiceException si falta la dirección del pedido, falla la
     *                          generación del PDF
     *                          o no se puede escribir el archivo en disco.
     */
    @Transactional
    public Invoice generateInvoice(long orderId) {
        // Guard: return existing invoice if one was already generated
        return invoiceRepository.findByOrderId(orderId)
                .orElseGet(() -> createInvoice(orderId));
    }

    /**
     * Devuelve todas las facturas del usuario indicado.
     *
     * @param userId ID del usuario propietario.
     * @return lista de entidades {@link Invoice} (puede estar vacía).
     */
    public List<Invoice> getInvoicesByUser(long userId) {
        List<Invoice> invoices = invoiceRepository.findAllByUserId(userId);
        if (invoices.isEmpty()) {
            throw new InvoiceException(InvoiceException.FAILED_FETCH);
        }
        return invoices;
    }

    /**
     * Recupera una factura por su número legible.
     *
     * @param invoiceNumber p. ej. {@code INV-2026-000042}.
     * @return la entidad {@link Invoice}.
     */
    public Invoice getByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceException(InvoiceException.DEFAULT));
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Invoice createInvoice(long orderId) {
        Order order = orderService.getOrderByIdHandler(orderId);
        User user = order.getUser();

        OrderAddresses orderAddr = orderAddrRepository.findByOrderId(orderId)
                .orElseThrow(() -> new InvoiceException(InvoiceException.ADDRESS_NOT_FOUND));

        String invoiceNumber = buildInvoiceNumber(orderId);

        // --- Build Thymeleaf context ---
        CompanyDTO companyDTO = buildCompanyDTO();
        InvoiceDataDTO invoiceDTO = buildInvoiceDataDTO(order, orderAddr, invoiceNumber);

        Context ctx = new Context();
        ctx.setVariable("company", companyDTO);
        ctx.setVariable("invoice", invoiceDTO);

        // --- Render HTML ---
        String html = templateEngine.process(TEMPLATE_NAME, ctx);

        // --- Convert to PDF ---
        byte[] pdfBytes = renderPdf(html);

        // --- Persist file ---
        String pdfPath = savePdf(pdfBytes, user.getId(), invoiceNumber);

        // --- Persist entity ---
        Invoice invoice = Invoice.builder()
                .order(order)
                .user(user)
                .invoiceNumber(invoiceNumber)
                .pdfPath(pdfPath)
                .status(InvoiceStatus.GENERATED)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(order.getTotalAmount())
                        .setScale(SCALE, RoundingMode.HALF_UP))
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Invoice {} generated for order {} (user {})", invoiceNumber, orderId, user.getId());
        return invoiceRepository.save(invoice);
    }

    // ---- invoice number -------------------------------------------------

    private String buildInvoiceNumber(long orderId) {
        return String.format("INV-%d-%06d", LocalDate.now().getYear(), orderId);
    }

    // ---- company DTO ----------------------------------------------------

    private CompanyDTO buildCompanyDTO() {
        return CompanyDTO.builder()
                .name(companyProps.getName())
                .nif(companyProps.getNif())
                .address(companyProps.getAddress())
                .email(companyProps.getEmail())
                .phone(companyProps.getPhone())
                .iban(companyProps.getIban())
                .primaryColor(companyProps.getPrimaryColor())
                .secondaryColor(companyProps.getSecondaryColor())
                .accentColor(companyProps.getAccentColor())
                .textColor(companyProps.getTextColor())
                .logoBase64(loadLogoBase64(companyProps.getLogoPath()))
                .build();
    }

    private String loadLogoBase64(String logoPath) {
        if (logoPath == null || logoPath.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(Path.of(logoPath));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.warn("Could not read company logo at '{}': {}", logoPath, e.getMessage());
            return null;
        }
    }

    // ---- invoice data DTO -----------------------------------------------

    private InvoiceDataDTO buildInvoiceDataDTO(Order order, OrderAddresses addr, String invoiceNumber) {
        LocalDate today = LocalDate.now();

        List<InvoiceLineDTO> lines = buildLines(order.getOrderItems());
        List<TaxSummaryDTO> taxes = buildTaxSummary(lines);

        BigDecimal totalBase = lines.stream()
                .map(InvoiceLineDTO::getBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = lines.stream()
                .map(InvoiceLineDTO::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = totalBase.add(totalTax);

        User user = order.getUser();

        InvoiceCustomerDTO customer = InvoiceCustomerDTO.builder()
                .name(user.getName() + " " + user.getSurname())
                .nif(user.getUsername()) // NIF not stored; username used as placeholder
                .address(buildAddressLine(addr))
                .postalCode(addr.getPostalCode())
                .city(addr.getCity())
                .country(addr.getCountry())
                .build();

        return InvoiceDataDTO.builder()
                .invoiceNumber(invoiceNumber)
                .issueDate(today.format(DATE_FMT))
                .operationDate(order.getCreatedAt().toLocalDate().format(DATE_FMT))
                .dueDate(today.plusDays(PAYMENT_DAYS).format(DATE_FMT))
                .paymentMethod(order.getPaymentMethod())
                .notes(null)
                .totalBase(totalBase.setScale(SCALE, RoundingMode.HALF_UP))
                .totalTax(totalTax.setScale(SCALE, RoundingMode.HALF_UP))
                .totalAmount(totalAmount.setScale(SCALE, RoundingMode.HALF_UP))
                .customer(customer)
                .lines(lines)
                .taxSummary(taxes)
                .build();
    }

    private String buildAddressLine(OrderAddresses addr) {
        String line = addr.getAddressLine1();
        if (addr.getAddressLine2() != null && !addr.getAddressLine2().isBlank()) {
            line += ", " + addr.getAddressLine2();
        }
        return line;
    }

    // ---- lines ----------------------------------------------------------

    private List<InvoiceLineDTO> buildLines(List<OrderItems> items) {
        List<InvoiceLineDTO> lines = new ArrayList<>();
        for (OrderItems item : items) {
            BigDecimal unitPrice = resolveUnitPrice(item);
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal taxRate = item.getTaxRate() != null ? item.getTaxRate() : BigDecimal.ZERO;

            BigDecimal baseAmount = unitPrice.multiply(qty)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal taxPercent = taxRate.multiply(BigDecimal.valueOf(100))
                    .setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal taxAmount = baseAmount.multiply(taxRate)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal total = baseAmount.add(taxAmount);

            lines.add(InvoiceLineDTO.builder()
                    .productName(item.getName())
                    .sku(item.getSku())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice.setScale(SCALE, RoundingMode.HALF_UP))
                    .baseAmount(baseAmount)
                    .taxPercent(taxPercent)
                    .taxAmount(taxAmount)
                    .totalAmount(total.setScale(SCALE, RoundingMode.HALF_UP))
                    .build());
        }
        return lines;
    }

    private BigDecimal resolveUnitPrice(OrderItems item) {
        if (item.getDiscountPrice() != null
                && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
            return item.getDiscountPrice();
        }
        return item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
    }

    // ---- tax summary ----------------------------------------------------

    private List<TaxSummaryDTO> buildTaxSummary(List<InvoiceLineDTO> lines) {
        // Group by taxPercent preserving order of first occurrence
        Map<BigDecimal, TaxSummaryDTO> taxMap = new LinkedHashMap<>();

        for (InvoiceLineDTO line : lines) {
            taxMap.compute(line.getTaxPercent(), (key, existing) -> {
                if (existing == null) {
                    return TaxSummaryDTO.builder()
                            .percent(key)
                            .base(line.getBaseAmount())
                            .amount(line.getTaxAmount())
                            .build();
                }
                existing.setBase(existing.getBase().add(line.getBaseAmount()));
                existing.setAmount(existing.getAmount().add(line.getTaxAmount()));
                return existing;
            });
        }
        return new ArrayList<>(taxMap.values());
    }

    // ---- PDF rendering --------------------------------------------------

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            throw new InvoiceException(InvoiceException.PDF_FAILED, e);
        }
    }

    // ---- file storage ---------------------------------------------------

    private String savePdf(byte[] pdfBytes, long userId, String invoiceNumber) {
        Path invoiceDir = Path.of(storagePath)
                .resolve(String.valueOf(userId))
                .resolve("invoices");
        Path target = invoiceDir.resolve(invoiceNumber + ".pdf");

        try {
            if (Files.notExists(invoiceDir)) {
                Files.createDirectories(invoiceDir);
            }
            Files.write(target, pdfBytes, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new InvoiceException(InvoiceException.STORAGE_ERROR, e);
        }

        return target.toString();
    }
}
