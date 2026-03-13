package es.marcha.backend.modules.order.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.order.application.dto.response.OrderItemsResponseDTO;
import es.marcha.backend.core.error.exception.OrderException;
import es.marcha.backend.modules.order.application.mapper.OrderItemMapper;
import es.marcha.backend.modules.order.domain.model.OrderItems;
import es.marcha.backend.modules.order.infrastructure.persistence.OrderItemRepository;
import jakarta.transaction.Transactional;

@Service
public class OrderItemsService {

    @Autowired
    private OrderItemRepository oItemRepository;

    public List<OrderItemsResponseDTO> getAllItemsFromOrder() {
        List<OrderItemsResponseDTO> itemsSnapshots = oItemRepository.findAll().stream()
                .map(OrderItemMapper::toOrderItemDTO)
                .toList();

        if (itemsSnapshots.isEmpty())
            throw new OrderException(OrderException.FAILED_ORDER_ADDRESSES);

        return itemsSnapshots;
    }

    public OrderItemsResponseDTO getOrderItemsByOrder(long id) {
        return oItemRepository.findById(id)
                .map(OrderItemMapper::toOrderItemDTO)
                .orElseThrow(() -> new OrderException(OrderException.FAILED_ORDER_ITEMS));
    }

    public OrderItems getOrderItemsByHandler(long id) {
        return oItemRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderException.FAILED_ORDER_ITEMS));
    }

    @Transactional
    public List<OrderItemsResponseDTO> saveOrderItems(List<OrderItems> orderItems) {
        if (orderItems == null || orderItems.isEmpty())
            throw new OrderException(OrderException.FAILED_ORDER_ITEMS);

        return orderItems.stream()
                .filter(Objects::nonNull)
                .peek(item -> item.setCreatedAt(LocalDateTime.now()))
                .map(oItemRepository::save)
                .map(OrderItemMapper::toOrderItemDTO)
                .toList();
    }

    /**
     * Recupera directamente los items de una orden por su ID.
     * Útil para forzar la inicialización de la colección lazy dentro de
     * un contexto transaccional antes de delegarlo a un hilo asíncrono.
     *
     * @param orderId el ID de la orden
     * @return lista de {@link OrderItems} asociados a esa orden
     */
    public List<OrderItems> getItemsByOrderId(long orderId) {
        return oItemRepository.findByOrderId(orderId);
    }

    /** NO METHOD FOR UPDATE AND DELETE - SNAPSHOTS ARE INMUTABLE */
}
