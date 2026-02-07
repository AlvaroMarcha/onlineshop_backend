package es.marcha.backend.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import es.marcha.backend.model.enums.OrderStatus;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.services.order.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {
    // Attribs
    @Autowired
    private OrderService oService;

    /**
     * Devuelve todas las órdenes existentes en la base de datos.
     *
     * @return ResponseEntity con la lista de {@link Order} y el estado HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = oService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    /**
     * Devuelve una orden específica según su identificador.
     *
     * @param id El ID de la orden que se desea recuperar.
     * @return ResponseEntity con la {@link Order} correspondiente y el estado HTTP
     *         200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable long id) {
        Order order = oService.getOrderById(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    /**
     * Avanza el estado de una orden según la lógica de negocio. Permite marcar la
     * orden como
     * cancelada o devuelta en casos especiales.
     *
     * @param orderId   El ID de la orden que se desea actualizar.
     * @param cancelled Si es {@code true}, la orden se marcará como
     *                  {@link OrderStatus#CANCELLED}.
     * @param returned  Si es {@code true} y la orden está entregada
     *                  ({@link OrderStatus#DELIVERED}),
     *                  la orden se marcará como {@link OrderStatus#RETURNED}.
     * @return ResponseEntity con el nuevo {@link OrderStatus} de la orden y el
     *         estado HTTP 202
     *         ACCEPTED.
     */
    @PostMapping("/nextStatus")
    public ResponseEntity<OrderStatus> nextOrderStatus(@RequestParam long orderId,
            @RequestParam boolean cancelled, @RequestParam boolean returned) {
        OrderStatus status = oService.nextStatus(orderId, cancelled, returned);
        return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
    }

}
