package es.marcha.backend.services.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.order.OrderAddrResponseDTO;
import es.marcha.backend.exception.OrderException;
import es.marcha.backend.mapper.order.OrderAddrMapper;
import es.marcha.backend.model.order.OrderAddresses;
import es.marcha.backend.repository.order.OrderAddrRepository;
import jakarta.transaction.Transactional;

@Service
public class OrderAddressService {

    @Autowired
    private OrderAddrRepository oAddrRepository;

    public List<OrderAddrResponseDTO> getAllOrderAddresses() {
        List<OrderAddrResponseDTO> orderSnapshots = oAddrRepository.findAll().stream()
                .map(OrderAddrMapper::toOrderAddressDTO).toList();

        if (orderSnapshots.isEmpty())
            throw new OrderException(OrderException.FAILED_ORDER_ADDRESSES);

        return orderSnapshots;
    }

    public OrderAddresses getOrderAddressById(long id) {
        return oAddrRepository.findByOrderId(id)
                .orElseThrow(() -> new OrderException(OrderException.FAILED_ORDER_ADDRESS));
    }

    public OrderAddrResponseDTO getOrderAddressByOrderId(long id) {
        return oAddrRepository.findByOrderId(id)
                .map(OrderAddrMapper::toOrderAddressDTO)
                .orElseThrow(() -> new OrderException(OrderException.FAILED_ORDER_ADDRESS));
    }

    @Transactional
    public OrderAddrResponseDTO saveOrderAddr(OrderAddresses orderAddress) {
        orderAddress.setCreatedAt(LocalDateTime.now());
        return OrderAddrMapper.toOrderAddressDTO(oAddrRepository.save(orderAddress));
    }

    /** NO METHOD FOR UPDATE AND DELETE - SNAPSHOTS ARE INMUTABLE */

}
