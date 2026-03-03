package es.marcha.backend.services.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.order.OrderItemRequestDTO;
import es.marcha.backend.dto.request.order.OrderRequestDTO;
import es.marcha.backend.dto.response.order.OrderAddrResponseDTO;
import es.marcha.backend.dto.response.order.OrderResponseDTO;
import org.springframework.context.annotation.Lazy;

import es.marcha.backend.exception.AddressException;
import es.marcha.backend.exception.InvoiceException;
import es.marcha.backend.exception.OrderException;
import es.marcha.backend.exception.ProductException;
import es.marcha.backend.services.invoice.InvoiceService;
import es.marcha.backend.mapper.order.OrderAddrMapper;
import es.marcha.backend.mapper.order.OrderMapper;
import es.marcha.backend.model.ecommerce.product.Product;
import es.marcha.backend.model.enums.OrderStatus;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.OrderItems;
import es.marcha.backend.model.user.Address;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.ecommerce.ProductRepository;
import es.marcha.backend.repository.order.OrderRepository;
import es.marcha.backend.repository.user.AddressRepository;
import es.marcha.backend.services.cart.CartService;
import es.marcha.backend.services.mail.UserEmailNotificationService;
import es.marcha.backend.services.user.UserService;
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
     * @throws OrderException si el usuario no tiene órdenes.
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

        if (orders == null || orders.isEmpty()) {
            throw new OrderException(OrderException.FAILED_FETCH);
        }

        return orders;
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
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);

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

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(total.doubleValue())
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = oRepository.save(order);

        items.forEach(item -> item.setOrder(savedOrder));
        oItemsService.saveOrderItems(items);
        savedOrder.setOrderItems(items);

        OrderAddrResponseDTO snapOrderAddress = oAddrService.saveOrderAddr(
                OrderAddrMapper.fromAddresstoOrderAddr(shippingAddress, savedOrder));

        OrderResponseDTO finalOrder = OrderMapper.toOrderDTO(savedOrder);
        finalOrder.setAddress(snapOrderAddress);

        userEmailNotificationService.sendOrderConfirmationEmail(user, savedOrder, items, snapOrderAddress);

        // Limpiar el carrito del usuario una vez el pedido queda confirmado
        cartService.clearCartByUserId(user.getId());

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

        // Si el pedido acaba de pasar a PAID, generar la factura PDF automáticamente
        // y persistirla en disco en la ruta del cliente
        if (currentStatus == OrderStatus.PAID) {
            try {
                invoiceService.generateInvoice(orderId);
                log.info("[OrderService] Factura generada automáticamente para el pedido {} al pasar a PAID.", orderId);
            } catch (InvoiceException e) {
                log.error("[OrderService] Error al generar la factura para el pedido {}: {}", orderId, e.getMessage(),
                        e);
            }
        }

        // Cargar los items dentro de la transacción para evitar
        // LazyInitializationException en el hilo async
        List<OrderItems> loadedItems = oItemsService.getItemsByOrderId(orderId);
        userEmailNotificationService.sendOrderStatusUpdateEmail(order.getUser(), order, loadedItems);
        return order.getStatus();
    }
}
