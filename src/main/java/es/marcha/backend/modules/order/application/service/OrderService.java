package es.marcha.backend.modules.order.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.order.application.dto.request.OrderItemRequestDTO;
import es.marcha.backend.modules.order.application.dto.request.OrderRequestDTO;
import es.marcha.backend.modules.order.application.dto.response.OrderAddrResponseDTO;
import es.marcha.backend.modules.order.application.dto.response.OrderResponseDTO;
import org.springframework.context.annotation.Lazy;

import es.marcha.backend.core.error.exception.AddressException;
import es.marcha.backend.core.error.exception.InvoiceException;
import es.marcha.backend.core.error.exception.OrderException;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.coupon.domain.model.Coupon;
import es.marcha.backend.modules.invoice.domain.model.Invoice;
import es.marcha.backend.modules.coupon.application.service.CouponService;
import es.marcha.backend.modules.invoice.application.service.InvoiceService;
import es.marcha.backend.modules.order.application.mapper.OrderAddrMapper;
import es.marcha.backend.modules.order.application.mapper.OrderMapper;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.core.shared.domain.enums.OrderStatus;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.domain.model.OrderItems;
import es.marcha.backend.core.user.domain.model.Address;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.order.infrastructure.persistence.OrderRepository;
import es.marcha.backend.core.user.infrastructure.persistence.AddressRepository;
import es.marcha.backend.modules.cart.application.service.CartService;
import es.marcha.backend.modules.catalog.application.service.InventoryService;
import es.marcha.backend.modules.catalog.domain.enums.MovementType;
import es.marcha.backend.modules.notification.application.service.UserEmailNotificationService;
import es.marcha.backend.core.user.application.service.UserService;
import jakarta.transaction.Transactional;

@Service
public class OrderService {
    // Attribs
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository oRepository;

    @Autowired
    private OrderAddressService oAddrService;

    @Autowired
    private OrderItemsService oItemsService;

    @Autowired
    private UserService uService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserEmailNotificationService userEmailNotificationService;

    @Autowired
    private CartService cartService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CouponService couponService;

    // Inyectado con @Lazy para evitar dependencia circular con InvoiceService
    // (InvoiceService -> OrderService -> InvoiceService)
    @Lazy
    @Autowired
    private InvoiceService invoiceService;

    // Methods
    /**
     * Obtiene una orden por su ID para uso interno de otros servicios.
     * Lanza una excepción si la orden no existe.
     *
     * @param id El ID de la orden a buscar.
     * @return La entidad {@link Order} correspondiente.
     * @throws OrderException si la orden no existe.
     */
    public Order getOrderByIdHandler(long id) {
        return oRepository.findById(id)
                .orElseThrow(() -> new OrderException());

    }

    /**
     * Obtiene todas las órdenes de un usuario, incluyendo el snapshot de dirección
     * de cada una.
     *
     * @param userId El ID del usuario cuyas órdenes se desean obtener.
     * @return Lista de {@link OrderResponseDTO} con las órdenes del usuario.
     *         Si no hay órdenes, retorna una lista vacía.
     */
    public List<OrderResponseDTO> getAllOrders(long userId) {
        List<OrderResponseDTO> orders = oRepository.findAllByUserId(userId).stream()
                .map(OrderMapper::toOrderDTO)
                .map(order -> {
                    OrderAddrResponseDTO addressSnapshot = oAddrService.getOrderAddressByOrderId(order.getId());
                    order.setAddress(addressSnapshot);
                    return order;
                })
                .toList();

        return orders;
    }

    /**
     * Obtiene todas las órdenes del sistema con paginación para el panel de
     * administración.
     * Incluye todas las órdenes independientemente de su estado o usuario.
     *
     * @param pageable Configuración de paginación y ordenamiento.
     * @return {@link Page} de {@link OrderResponseDTO} con las órdenes paginadas.
     */
    public Page<OrderResponseDTO> getAllOrdersForAdmin(Pageable pageable) {
        return oRepository.findAllWithUser(pageable)
                .map(OrderMapper::toOrderDTO)
                .map(order -> {
                    try {
                        OrderAddrResponseDTO addressSnapshot = oAddrService.getOrderAddressByOrderId(order.getId());
                        order.setAddress(addressSnapshot);
                    } catch (Exception e) {
                        // Si no hay dirección de envío, continuar sin ella
                        log.warn("No se pudo cargar la dirección para la orden {}: {}", order.getId(), e.getMessage());
                    }
                    return order;
                });
    }

    /**
     * Crea una nueva orden calculando el totalAmount desde los precios reales de la
     * BD.
     * El precio unitario se snapshot-ea en cada OrderItem en el momento de la
     * compra.
     *
     * @param request DTO con userId y lista de items (productId + quantity).
     * @return {@link OrderResponseDTO} con la orden creada y el total calculado.
     * @throws OrderException   si el usuario no existe o no tiene dirección por
     *                          defecto.
     * @throws ProductException si algún producto no existe o no está activo.
     */
    @Transactional
    public OrderResponseDTO saveNewOrder(OrderRequestDTO request) {
        User user = uService.getUserByIdForHandler(request.getUserId());

        List<Address> addresses = user.getAddresses();
        if (addresses == null || addresses.isEmpty())
            throw new OrderException(OrderException.USER_ADDRESS_LENGHT_0);

        // Resolver la dirección de envío: si el cliente elige una concreta se valida
        // que le pertenezca; si no envía addressId se usa la predeterminada.
        Address shippingAddress;
        if (request.getAddressId() != null) {
            shippingAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AddressException(AddressException.DEFAULT));
            // Seguridad: la dirección debe pertenecer al usuario que crea el pedido
            if (shippingAddress.getUser().getId() != user.getId())
                throw new AddressException(AddressException.DEFAULT);
        } else {
            shippingAddress = addresses.stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElseThrow(() -> new OrderException(OrderException.USER_ADDRESS_LENGHT_0));
        }

        // Construir snapshot de items + calcular total en un único paso
        List<OrderItems> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

            if (!product.isActive())
                throw new ProductException(ProductException.DEFAULT);

            // Verificar stock disponible: no se puede comprar más unidades de las
            // existentes
            if (product.getStock() <= 0 || product.getStock() < itemDto.getQuantity())
                throw new ProductException(ProductException.INSUFFICIENT_STOCK);

            // Decrementar stock atómicamente dentro de la transacción
            int previousStock = product.getStock();
            int newStock = previousStock - itemDto.getQuantity();
            product.setStock(newStock);
            productRepository.save(product);

            // Registrar movimiento SALE y sincronizar inventario
            inventoryService.recordMovementInternal(
                    product, itemDto.getQuantity(), previousStock, newStock,
                    MovementType.SALE,
                    "Venta generada por pedido", "SYSTEM");

            BigDecimal effectivePrice = (product.getDiscountPrice() != null
                    && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? product.getDiscountPrice()
                            : product.getPrice();

            total = total.add(effectivePrice.multiply(BigDecimal.valueOf(itemDto.getQuantity())));

            items.add(OrderItems.builder()
                    .product(product)
                    .name(product.getName())
                    .description(product.getDescription())
                    .sku(product.getSku())
                    .price(product.getPrice())
                    .discountPrice(product.getDiscountPrice())
                    .quantity(itemDto.getQuantity())
                    .weight(product.getWeight())
                    .isDigital(product.isDigital())
                    .isFeatured(product.isFeatured())
                    .taxRate(product.getTaxRate())
                    .isActive(product.isActive())
                    .isDeleted(product.isDeleted())
                    .soldCount(product.getSoldCount())
                    .build());
        }

        // Base imponible bruta (suma de precio_base × cantidad por ítem)
        BigDecimal rawBase = total;

        // Aplicar cupón sobre la base imponible
        Coupon appliedCoupon = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountedBase = rawBase;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            appliedCoupon = couponService.findAndValidate(request.getCouponCode(), rawBase, user.getId());
            discountAmount = couponService.calculateDiscount(appliedCoupon, rawBase);
            discountedBase = rawBase.subtract(discountAmount).max(BigDecimal.ZERO);
        }

        // IVA recalculado sobre la base con descuento, prorrateando entre ítems
        BigDecimal discountRatio = rawBase.compareTo(BigDecimal.ZERO) > 0
                ? discountedBase.divide(rawBase, 10, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        BigDecimal totalVat = BigDecimal.ZERO;
        for (OrderItems item : items) {
            BigDecimal unitPrice = (item.getDiscountPrice() != null
                    && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? item.getDiscountPrice()
                            : item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            BigDecimal itemBase = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal itemDiscountedBase = itemBase.multiply(discountRatio)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal taxRate = item.getTaxRate() != null ? item.getTaxRate() : BigDecimal.ZERO;
            totalVat = totalVat.add(itemDiscountedBase.multiply(taxRate)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
        }

        BigDecimal totalAmount = discountedBase.add(totalVat).setScale(2, java.math.RoundingMode.HALF_UP);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(totalAmount.doubleValue())
                .discountAmount(discountAmount.doubleValue())
                .couponId(appliedCoupon != null ? appliedCoupon.getId() : null)
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = oRepository.save(order);

        // Incrementar el contador de usos del cupón una vez el pedido queda guardado
        if (appliedCoupon != null) {
            couponService.incrementUsedCount(appliedCoupon.getId(), user.getId());
        }

        items.forEach(item -> item.setOrder(savedOrder));
        oItemsService.saveOrderItems(items);
        savedOrder.setOrderItems(items);

        OrderAddrResponseDTO snapOrderAddress = oAddrService.saveOrderAddr(
                OrderAddrMapper.fromAddresstoOrderAddr(shippingAddress, savedOrder));

        OrderResponseDTO finalOrder = OrderMapper.toOrderDTO(savedOrder);
        finalOrder.setAddress(snapOrderAddress);

        userEmailNotificationService.sendOrderConfirmationEmail(user, savedOrder, items, snapOrderAddress);

        // Limpiar el carrito del usuario una vez el pedido queda confirmado
        cartService.clearCartByUsername(user.getUsername());

        return finalOrder;
    }

    /**
     * Persiste una orden directamente en la base de datos sin inicialización
     * adicional.
     * Destinado a uso interno de otros servicios que necesiten actualizar el estado
     * de una orden.
     *
     * @param order La entidad {@link Order} a guardar.
     * @return La entidad {@link Order} persistida.
     */
    public Order saveOrder(Order order) {
        return oRepository.save(order);
    }

    /**
     * Maneja la transición de una orden al estado PAID: genera la factura PDF
     * y envía el email de confirmación con la factura adjunta.
     * <p>
     * Este método se invoca tanto desde {@code nextStatus()} como desde
     * {@code PaymentService.updateOrderStatusFromPayments()} para garantizar
     * que la factura se genere independientemente del flujo (webhook o manual).
     * </p>
     *
     * @param orderId ID de la orden que acaba de pasar a PAID
     */
    public void handleOrderPaidTransition(long orderId) {
        log.info("[OrderService] ========== handleOrderPaidTransition iniciado para orderId={} ==========", orderId);
        Order order = getOrderByIdHandler(orderId);
        log.info("[OrderService] Orden recuperada: id={}, status={}, userId={}",
                order.getId(), order.getStatus(), order.getUser().getId());
        List<OrderItems> loadedItems = oItemsService.getItemsByOrderId(orderId);
        log.info("[OrderService] Items de la orden cargados: {} items", loadedItems.size());

        try {
            log.info("[OrderService] Intentando generar factura para orderId={}...", orderId);
            Invoice invoice = invoiceService.generateInvoice(orderId);
            log.info("[OrderService] ✓ Factura {} generada automáticamente para el pedido {} al pasar a PAID.",
                    invoice.getInvoiceNumber(), orderId);
            log.info("[OrderService] Ruta del PDF: {}", invoice.getPdfPath());
            log.info("[OrderService] Enviando email con factura adjunta...");
            userEmailNotificationService.sendOrderStatusUpdateEmailWithInvoice(
                    order.getUser(), order, loadedItems,
                    invoice.getPdfPath(),
                    invoice.getInvoiceNumber() + ".pdf");
            log.info("[OrderService] Email con factura enviado exitosamente");
        } catch (InvoiceException e) {
            log.error(
                    "[OrderService] ✗ ERROR al generar la factura para el pedido {} — se enviará el email sin adjunto",
                    orderId, e);
            log.error("[OrderService] Tipo de excepción: {}", e.getClass().getName());
            log.error("[OrderService] Mensaje: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("[OrderService] Causa raíz: {}", e.getCause().getMessage(), e.getCause());
            }
            // Fallback: enviar email normal sin adjunto si la factura falla
            log.info("[OrderService] Enviando email SIN adjunto como fallback...");
            userEmailNotificationService.sendOrderStatusUpdateEmail(order.getUser(), order, loadedItems);
            log.info("[OrderService] Email sin adjunto enviado");
        } catch (Exception e) {
            log.error("[OrderService] ✗ ERROR INESPERADO al procesar transición a PAID para orderId={}",
                    orderId, e);
            throw e;
        }
        log.info("[OrderService] ========== handleOrderPaidTransition finalizado para orderId={} ==========", orderId);
    }

    /**
     * Avanza el estado de una orden según la lógica de negocio definida.
     * <p>
     * Este método recupera la orden por su ID y actualiza su estado según las
     * siguientes reglas:
     * <ul>
     * <li>Si {@code isCancelled} es {@code true}, el estado se establece en
     * {@link OrderStatus#CANCELLED} inmediatamente.</li>
     * <li>Si {@code isReturned} es {@code true} y el estado actual es
     * {@link OrderStatus#DELIVERED}, el estado se cambia a
     * {@link OrderStatus#RETURNED}.</li>
     * <li>En condiciones normales, el estado se avanza secuencialmente según la
     * transición
     * definida:
     * <ul>
     * <li>{@link OrderStatus#CREATED} -> {@link OrderStatus#PAID}</li>
     * <li>{@link OrderStatus#PAID} -> {@link OrderStatus#PROCESSING}</li>
     * <li>{@link OrderStatus#PROCESSING} -> {@link OrderStatus#SHIPPED}</li>
     * <li>{@link OrderStatus#SHIPPED} -> {@link OrderStatus#DELIVERED}</li>
     * </ul>
     * </li>
     * </ul>
     * <p>
     * La orden se guarda automáticamente tras actualizar su estado. Este método
     * está marcado con
     * {@link jakarta.transaction.Transactional} para asegurar la consistencia en la
     * base de datos.
     *
     * @param orderId     el ID de la orden cuyo estado se desea actualizar
     * @param isCancelled indica si la orden debe ser marcada como cancelada
     * @param isReturned  indica si la orden entregada debe ser marcada como
     *                    devuelta
     * @return el nuevo estado de la orden después de aplicar las reglas de
     *         transición
     * @throws IllegalArgumentException si no existe ninguna orden con el ID
     *                                  proporcionado
     */
    @Transactional
    public OrderStatus nextStatus(long orderId, boolean isCancelled, boolean isReturned) {
        Order order = getOrderByIdHandler(orderId);
        OrderStatus currentStatus = order.getStatus();

        // Error 2: no cancelar órdenes ya en estado terminal
        if (isCancelled) {
            if (currentStatus == OrderStatus.CANCELLED
                    || currentStatus == OrderStatus.RETURNED
                    || currentStatus == OrderStatus.DELIVERED) {
                throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
            }
            // Error 1: persistir el cambio antes de retornar
            order.setStatus(OrderStatus.CANCELLED);
            saveOrder(order);
            // Cargar los items dentro de la transacción para evitar
            // LazyInitializationException en el hilo async
            List<OrderItems> cancelledItems = oItemsService.getItemsByOrderId(orderId);

            // Restaurar stock y registrar movimiento RETURN por cada ítem cancelado
            for (OrderItems item : cancelledItems) {
                Product p = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (p != null) {
                    int prevStock = p.getStock();
                    int restoredStock = prevStock + item.getQuantity();
                    p.setStock(restoredStock);
                    p.setUpdatedAt(java.time.LocalDateTime.now());
                    productRepository.save(p);
                    inventoryService.recordMovementInternal(
                            p, item.getQuantity(), prevStock, restoredStock,
                            MovementType.RETURN,
                            "Devolución por cancelación del pedido #" + orderId, "SYSTEM");
                }
            }

            userEmailNotificationService.sendOrderStatusUpdateEmail(order.getUser(), order, cancelledItems);
            return order.getStatus();
        }

        if (currentStatus != null) {
            switch (currentStatus) {
                case CREATED -> currentStatus = OrderStatus.PAID;
                case PAID -> currentStatus = OrderStatus.PROCESSING;
                case PROCESSING -> currentStatus = OrderStatus.SHIPPED;
                case SHIPPED -> currentStatus = OrderStatus.DELIVERED;
                case DELIVERED -> {
                    if (isReturned) {
                        // Error 1: persistir el cambio antes de retornar
                        order.setStatus(OrderStatus.RETURNED);
                        saveOrder(order);
                        // Cargar los items dentro de la transacción para evitar
                        // LazyInitializationException en el hilo async
                        List<OrderItems> returnedItems = oItemsService.getItemsByOrderId(orderId);

                        // Restaurar stock y registrar movimiento RETURN por cada ítem devuelto
                        for (OrderItems item : returnedItems) {
                            Product p = productRepository.findById(item.getProduct().getId()).orElse(null);
                            if (p != null) {
                                int prevStock = p.getStock();
                                int restoredStock = prevStock + item.getQuantity();
                                p.setStock(restoredStock);
                                p.setUpdatedAt(java.time.LocalDateTime.now());
                                productRepository.save(p);
                                inventoryService.recordMovementInternal(
                                        p, item.getQuantity(), prevStock, restoredStock,
                                        MovementType.RETURN,
                                        "Devolución por retorno del pedido #" + orderId, "SYSTEM");
                            }
                        }

                        userEmailNotificationService.sendOrderStatusUpdateEmail(order.getUser(), order, returnedItems);
                        return order.getStatus();
                    }
                    // Error 3: lanzar excepción si DELIVERED sin isReturned
                    throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
                }
                // Error 3: estados terminales lanzan excepción en lugar de log silencioso
                default -> throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
            }
        }
        order.setStatus(currentStatus);
        saveOrder(order);

        // Cargar los items dentro de la transacción para evitar
        // LazyInitializationException en el hilo async
        List<OrderItems> loadedItems = oItemsService.getItemsByOrderId(orderId);

        // Si el pedido acaba de pasar a PAID, generar la factura PDF automáticamente
        // y enviar el email con la factura adjunta
        if (currentStatus == OrderStatus.PAID) {
            handleOrderPaidTransition(orderId);
            return order.getStatus();
        }

        userEmailNotificationService.sendOrderStatusUpdateEmail(order.getUser(), order, loadedItems);
        return order.getStatus();
    }
}
