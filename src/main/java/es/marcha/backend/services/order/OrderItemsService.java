package es.marcha.backend.services.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.order.OrderItemsResponseDTO;
import es.marcha.backend.exception.OrderException;
import es.marcha.backend.mapper.OrderItemMapper;
import es.marcha.backend.model.order.OrderItems;
import es.marcha.backend.repository.order.OrderItemRepository;
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

    /** NO METHOD FOR UPDATE AND DELETE - SNAPSHOTS ARE INMUTABLE */
}
