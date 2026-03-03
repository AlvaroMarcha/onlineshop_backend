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

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;

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
import es.marcha.backend.services.media.MediaService;
import es.marcha.backend.services.order.OrderService;
import jakarta.annotation.PostConstruct;
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
    private final MediaService mediaService;

    @Value("${app.invoices.storage-path}")
    private String storagePath;

    public InvoiceService(OrderService orderService,
            OrderAddrRepository orderAddrRepository,
            InvoiceRepository invoiceRepository,
            SpringTemplateEngine templateEngine,
            CompanyProperties companyProps,
            MediaService mediaService) {
        this.orderService = orderService;
        this.orderAddrRepository = orderAddrRepository;
        this.invoiceRepository = invoiceRepository;
        this.templateEngine = templateEngine;
        this.companyProps = companyProps;
        this.mediaService = mediaService;
    }

    @PostConstruct
    private void logStoragePath() {
        log.info("[InvoiceService] PDF storage path resolved to: {}", storagePath);
        log.info("[InvoiceService] Company address loaded: '{}'", companyProps.getAddress());
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
        return invoiceRepository.findByOrderId(orderId)
                .map(existing -> {
                    // Si la entidad existe pero el PDF no está en disco, regenerar solo el archivo
                    boolean pdfMissing = existing.getPdfPath() == null
                            || Files.notExists(Path.of(existing.getPdfPath()));
                    if (pdfMissing) {
                        log.warn(
                                "[InvoiceService] Factura {} existe en BD pero el PDF no está en disco ({}). Regenerando archivo.",
                                existing.getInvoiceNumber(), existing.getPdfPath());
                        Order order = orderService.getOrderByIdHandler(orderId);
                        OrderAddresses addr = orderAddrRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new InvoiceException(InvoiceException.ADDRESS_NOT_FOUND));
                        String newPath = regeneratePdfFile(existing.getInvoiceNumber(), order, addr,
                                existing.getUser().getId(), existing.getId());
                        existing.setPdfPath(newPath);
                        return invoiceRepository.save(existing);
                    }
                    return existing;
                })
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

    /**
     * Lee el PDF de la factura indicada desde disco y devuelve sus bytes.
     *
     * @param invoiceNumber n&#250;mero de la factura (p. ej.
     *                      {@code INV-2026-000042}).
     * @return array de bytes del PDF generado.
     * @throws InvoiceException si la factura no existe o el archivo no se puede
     *                          leer.
     */
    public byte[] getPdfBytes(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceException(InvoiceException.DEFAULT));
        Path pdfPath = Path.of(invoice.getPdfPath());
        if (Files.notExists(pdfPath)) {
            throw new InvoiceException(InvoiceException.STORAGE_ERROR);
        }
        try {
            return Files.readAllBytes(pdfPath);
        } catch (IOException e) {
            throw new InvoiceException(InvoiceException.STORAGE_ERROR, e);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String regeneratePdfFile(String invoiceNumber, Order order, OrderAddresses addr, long userId,
            long invoiceId) {
        CompanyDTO companyDTO = buildCompanyDTO();
        InvoiceDataDTO invoiceDTO = buildInvoiceDataDTO(order, addr, invoiceNumber, invoiceId);
        Context ctx = new Context();
        ctx.setVariable("company", companyDTO);
        ctx.setVariable("invoice", invoiceDTO);
        String html = templateEngine.process(TEMPLATE_NAME, ctx);
        byte[] pdfBytes = renderPdf(html);
        return savePdf(pdfBytes, userId, invoiceNumber, invoiceId);
    }

    private Invoice createInvoice(long orderId) {
        Order order = orderService.getOrderByIdHandler(orderId);
        User user = order.getUser();

        OrderAddresses orderAddr = orderAddrRepository.findByOrderId(orderId)
                .orElseThrow(() -> new InvoiceException(InvoiceException.ADDRESS_NOT_FOUND));

        String invoiceNumber = buildInvoiceNumber();

        // --- Persistir la entidad primero para obtener el ID de BD ---
        // El PDF se genera después, usando el ID como parte del nombre del archivo
        Invoice invoice = Invoice.builder()
                .order(order)
                .user(user)
                .invoiceNumber(invoiceNumber)
                .pdfPath(null)
                .status(InvoiceStatus.GENERATED)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(order.getTotalAmount())
                        .setScale(SCALE, RoundingMode.HALF_UP))
                .createdAt(LocalDateTime.now())
                .build();
        invoice = invoiceRepository.save(invoice);
        long invoiceId = invoice.getId();

        // --- Build Thymeleaf context (incluye el ID interno de BD) ---
        CompanyDTO companyDTO = buildCompanyDTO();
        InvoiceDataDTO invoiceDTO = buildInvoiceDataDTO(order, orderAddr, invoiceNumber, invoiceId);

        Context ctx = new Context();
        ctx.setVariable("company", companyDTO);
        ctx.setVariable("invoice", invoiceDTO);

        // --- Render HTML ---
        String html = templateEngine.process(TEMPLATE_NAME, ctx);

        // --- Convert to PDF ---
        byte[] pdfBytes = renderPdf(html);

        // --- Guardar PDF con el ID de BD en el nombre del archivo ---
        String pdfPath = savePdf(pdfBytes, user.getId(), invoiceNumber, invoiceId);

        // --- Actualizar la entidad con la ruta del PDF generado ---
        invoice.setPdfPath(pdfPath);
        log.info("Invoice {} (id={}) generated for order {} (user {})", invoiceNumber, invoiceId, orderId,
                user.getId());
        return invoiceRepository.save(invoice);
    }

    // ---- invoice number -------------------------------------------------

    /**
     * Genera el siguiente número de factura correlativo para el año en curso.
     * Consulta la última factura del año con bloqueo pesimista para garantizar
     * que no se producen huecos ni duplicados, cumpliendo el art. 6 del
     * RD 1619/2012 (reglamento de facturación español).
     * El formato resultante es {@code INV-YYYY-NNNNNN} (p. ej.
     * {@code INV-2026-000001}).
     */
    private synchronized String buildInvoiceNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "INV-" + year + "-";
        List<Invoice> last = invoiceRepository.findLastByYearPrefix(prefix);
        int nextSeq = 1;
        if (!last.isEmpty()) {
            String lastNumber = last.get(0).getInvoiceNumber();
            try {
                nextSeq = Integer.parseInt(lastNumber.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("[InvoiceService] No se pudo parsear el secuencial de '{}', usando siguiente disponible.",
                        lastNumber);
                nextSeq = last.size() + 1;
            }
        }
        return String.format("%s%06d", prefix, nextSeq);
    }

    // ---- company DTO ----------------------------------------------------

    private CompanyDTO buildCompanyDTO() {
        String[] logoData = loadLogoBase64();
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
                .logoBase64(logoData != null ? logoData[0] : null)
                .logoMime(logoData != null ? logoData[1] : null)
                .build();
    }

    /**
     * Carga el logo de la empresa como Base64, buscando primero entre los logos
     * subidos dinámicamente (PNG/JPG/JPEG) via MediaService y cayendo en el
     * path estático de CompanyProperties como fallback.
     *
     * @return array [base64, mimeType] o {@code null} si no hay logo disponible
     */
    private String[] loadLogoBase64() {
        // 1. Logo subido via POST /company/logo (búsqueda dinámica multi-extensión)
        var logoResource = mediaService.getCompanyLogoResource();
        if (logoResource.isPresent()) {
            try {
                byte[] bytes = Files.readAllBytes(logoResource.get().getFile().toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String filename = logoResource.get().getFilename();
                String mime = filename != null && filename.toLowerCase().endsWith(".png")
                        ? "image/png"
                        : "image/jpeg";
                return new String[] { base64, mime };
            } catch (IOException e) {
                log.warn("[InvoiceService] No se pudo leer el logo dinámico: {}", e.getMessage());
            }
        }
        // 2. Fallback: COMPANY_LOGO_PATH env var
        String logoPath = companyProps.getLogoPath();
        if (logoPath != null && !logoPath.isBlank()) {
            try {
                byte[] bytes = Files.readAllBytes(Path.of(logoPath));
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String mime = logoPath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                return new String[] { base64, mime };
            } catch (IOException e) {
                log.warn("[InvoiceService] No se pudo leer el logo de COMPANY_LOGO_PATH '{}': {}", logoPath,
                        e.getMessage());
            }
        }
        return null;
    }

    // ---- invoice data DTO -----------------------------------------------

    private InvoiceDataDTO buildInvoiceDataDTO(Order order, OrderAddresses addr, String invoiceNumber,
            long internalId) {
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

        // NIF/DNI se deja vacío hasta que se implemente el campo en el registro de
        // usuario
        InvoiceCustomerDTO customer = InvoiceCustomerDTO.builder()
                .name(user.getName() + " " + user.getSurname())
                .nif(null)
                .address(buildAddressLine(addr))
                .postalCode(addr.getPostalCode())
                .city(addr.getCity())
                .country(addr.getCountry())
                .build();

        return InvoiceDataDTO.builder()
                .internalId(internalId)
                .orderId(order.getId())
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

    /**
     * Renderiza el HTML a PDF registrando fuentes del sistema con soporte
     * completo de caracteres latinos (&#241;, acentos, etc.).
     * Detecta autom&#225;ticamente las rutas seg&#250;n el sistema operativo
     * (Windows o Linux/Alpine).
     */
    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            registerFonts(builder);
            // Usar Jsoup + W3CDom para parsear el HTML como String Java puro,
            // evitando cualquier conversi&#243;n de bytes que pueda corromper caracteres
            // latinos (&#241;, acentos, etc.) durante el procesado XHTML interno.
            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
            jsoupDoc.outputSettings().charset(java.nio.charset.StandardCharsets.UTF_8);
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
            builder.withW3cDocument(w3cDoc, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            log.error("[InvoiceService] Fallo al generar el PDF: {}", e.getMessage(), e);
            throw new InvoiceException(InvoiceException.PDF_FAILED, e);
        }
    }

    private void registerFonts(PdfRendererBuilder builder) {
        // Rutas candidatas: Windows (Arial) y Linux/Alpine (DejaVu)
        String[][] candidates = {
                { "C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/arialbd.ttf" },
                { "/usr/share/fonts/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf" },
                { "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" },
                { "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf" }
        };

        for (String[] entry : candidates) {
            java.io.File regular = new java.io.File(entry[0]);
            if (!regular.exists())
                continue;

            // Peso 400 = normal, peso 700 = bold — sin esto openhtmltopdf
            // sintetiza la negrita artificialmente (resultado borroso)
            builder.useFont(regular, "Arial", 400,
                    BaseRendererBuilder.FontStyle.NORMAL, true);

            java.io.File bold = new java.io.File(entry[1]);
            if (bold.exists()) {
                builder.useFont(bold, "Arial", 700,
                        BaseRendererBuilder.FontStyle.NORMAL, true);
            }
            log.info("[InvoiceService] Fuente registrada: {}", entry[0]);
            return;
        }
        log.warn(
                "[InvoiceService] No se encontro fuente del sistema. Los caracteres especiales pueden no renderizarse correctamente.");
    }

    // ---- file storage ---------------------------------------------------

    private String savePdf(byte[] pdfBytes, long userId, String invoiceNumber, long invoiceId) {
        Path invoiceDir = Path.of(storagePath)
                .resolve(String.valueOf(userId))
                .resolve("invoices");
        // Nombre: {invoiceNumber}_{invoiceId}.pdf — el número legal no cambia,
        // el ID de BD se añade al nombre del archivo para identificación rápida
        Path target = invoiceDir.resolve(invoiceNumber + "_" + invoiceId + ".pdf");

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
