package es.marcha.backend.controller.snapshots;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.response.order.OrderAddrResponseDTO;
import es.marcha.backend.model.order.OrderAddresses;
import es.marcha.backend.services.order.OrderAddressService;

@RestController
@RequestMapping("/orders/history")
public class OrderAddrController {

    @Autowired
    private OrderAddressService oAddrService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderAddrResponseDTO> getOrderAddrById(@PathVariable long id) {
        OrderAddrResponseDTO orderAddress = oAddrService.getOrderAddressById(id);
        return new ResponseEntity<>(orderAddress, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<OrderAddrResponseDTO>> getAllOrderAddr() {
        List<OrderAddrResponseDTO> ordersAddresses = oAddrService.getAllOrderAddresses();
        return new ResponseEntity<>(ordersAddresses, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OrderAddrResponseDTO> createOrderAddr(@RequestBody OrderAddresses orderAddress) {
        OrderAddrResponseDTO createdOrderAddr = oAddrService.saveOrderAddr(orderAddress);
        return new ResponseEntity<>(createdOrderAddr, HttpStatus.OK);
    }

}
