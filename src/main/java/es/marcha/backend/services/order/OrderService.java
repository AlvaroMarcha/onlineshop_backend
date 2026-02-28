package es.marcha.backend.services.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.order.OrderAddrResponseDTO;
import es.marcha.backend.dto.response.order.OrderResponseDTO;
import es.marcha.backend.exception.OrderException;
import es.marcha.backend.mapper.order.OrderAddrMapper;
import es.marcha.backend.mapper.order.OrderMapper;
import es.marcha.backend.model.enums.OrderStatus;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.user.Address;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.order.OrderRepository;
import es.marcha.backend.services.user.UserService;
import jakarta.transaction.Transactional;

@Service
public class OrderService {
    // Attribs
    @Autowired
    private OrderRepository oRepository;

    @Autowired
    private OrderAddressService oAddrService;

    @Autowired
    private OrderItemsService oItemsService;

    @Autowired
    private UserService uService;

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
     * Obtiene todas las órdenes de un usuario, incluyendo el snapshot de dirección de cada una.
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

    // CREAR SNAPSHOT!!!
    @Transactional
    public OrderResponseDTO saveNewOrder(Order order) {
        User user = uService.getUserByIdForHandler(order.getUser().getId());

        List<Address> addresses = user.getAddresses();

        if (addresses.size() == 0 || addresses == null)
            throw new OrderException(OrderException.USER_ADDRESS_LENGHT_0);

        Address addressDefault = addresses.stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElseThrow(() -> new OrderException(OrderException.USER_ADDRESS_LENGHT_0));

        // SNAPSHOTS
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = oRepository.save(order);

        order.getOrderItems().forEach(item -> item.setOrder(savedOrder));
        oItemsService.saveOrderItems(order.getOrderItems());

        OrderAddrResponseDTO snapOrderAddress = oAddrService.saveOrderAddr(
                OrderAddrMapper.fromAddresstoOrderAddr(
                        addressDefault,
                        savedOrder));

        OrderResponseDTO finalOrder = OrderMapper.toOrderDTO(savedOrder);
        finalOrder.setAddress(snapOrderAddress);

        return finalOrder;
    }

    /**
     * Persiste una orden directamente en la base de datos sin inicialización adicional.
     * Destinado a uso interno de otros servicios que necesiten actualizar el estado de una orden.
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
        if (isCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
            return order.getStatus();
        }

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus != null) {
            switch (currentStatus) {
                case CREATED -> currentStatus = OrderStatus.PAID;
                case PAID -> currentStatus = OrderStatus.PROCESSING;
                case PROCESSING -> currentStatus = OrderStatus.SHIPPED;
                case SHIPPED -> currentStatus = OrderStatus.DELIVERED;
                case DELIVERED -> {
                    if (isReturned) {
                        order.setStatus(OrderStatus.RETURNED);
                        return order.getStatus();
                    }
                }
                default -> {
                    System.err.println("Wrong status: " + currentStatus);
                }
            }
        }
        order.setStatus(currentStatus);
        saveOrder(order);
        return order.getStatus();
    }
}
