package es.marcha.backend.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.response.order.OrderResponseDTO;
import es.marcha.backend.dto.response.order.PaymentResponseDTO;
import es.marcha.backend.exception.OrderException;
import es.marcha.backend.model.enums.OrderStatus;
import es.marcha.backend.model.enums.PaymentStatus;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.Payment;
import es.marcha.backend.services.order.OrderService;
import es.marcha.backend.services.order.PaymentService;

@RestController
@RequestMapping("/orders")
public class OrderController {
    // Attribs
    @Autowired
    private OrderService oService;
    @Autowired
    private PaymentService pService;

    /**
     * Devuelve todas las órdenes existentes en la base de datos.
     *
     * @return ResponseEntity con la lista de {@link Order} y el estado HTTP 200 OK.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(@PathVariable long id) {
        List<OrderResponseDTO> orders = oService.getAllOrders(id);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody Order order) {
        OrderResponseDTO newOrder = oService.saveNewOrder(order);
        return new ResponseEntity<>(newOrder, HttpStatus.OK);
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
    @PostMapping("/next-status")
    public ResponseEntity<OrderStatus> nextOrderStatus(@RequestParam long orderId,
            @RequestParam boolean cancelled, @RequestParam boolean returned) {
        OrderStatus status = oService.nextStatus(orderId, cancelled, returned);
        return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
    }

    /*
     * PAYMENTS
     */

    /**
     * Crea un nuevo pago asociado a una orden concreta.
     *
     * Este endpoint permite registrar un nuevo intento de pago para una orden
     * existente. El pago se asocia a la orden indicada por {@code orderId} y se
     * inicializa con el estado correspondiente según la lógica del servicio.
     *
     * Flujo:
     * 1. Se obtiene la Order a partir del {@code orderId}.
     * 2. Se asocia la Order al Payment recibido en el body.
     * 3. Se delega la creación del pago al {@link PaymentService}.
     * 4. Se devuelve el pago creado como {@link PaymentResponseDTO}.
     *
     * @param orderId ID de la orden a la que pertenece el pago.
     * @param payment Datos del pago a crear (amount, provider, transactionId,
     *                etc.).
     * @return ResponseEntity con el {@link PaymentResponseDTO} del pago creado
     *         y estado HTTP 201 CREATED.
     */
    @PostMapping("/{orderId}/payments")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @PathVariable long orderId,
            @RequestBody Payment payment) {
        payment.setOrder(oService.getOrderByIdHandler(orderId));
        PaymentResponseDTO savedPayment = pService.savePayment(payment);
        return new ResponseEntity<>(savedPayment, HttpStatus.CREATED);
    }

    /**
     * Obtiene el último pago válido asociado a una orden.
     *
     * Se considera "pago válido" aquel cuyo estado forma parte del ciclo
     * relevante del pago (por ejemplo: PENDING, AUTHORIZED, SUCCESS, REFUNDED,
     * etc.).
     *
     * Si existen múltiples pagos válidos, se devuelve el más reciente
     * según la fecha de creación.
     *
     * Este endpoint evita que el frontend tenga que aplicar lógica de negocio
     * para decidir qué pago es el actual.
     *
     * @param orderId ID de la orden de la cual se desea obtener el último pago
     *                válido.
     * @return ResponseEntity con el {@link PaymentResponseDTO} del último pago
     *         válido
     *         y estado HTTP 200 OK.
     * @throws OrderException si la orden no tiene pagos válidos.
     */
    @GetMapping("/{orderId}/payments/last")
    public ResponseEntity<PaymentResponseDTO> getLastValidPayment(
            @PathVariable long orderId) {
        PaymentResponseDTO lastPayment = pService.getLastValidPaymentByOrderId(orderId);
        return new ResponseEntity<>(lastPayment, HttpStatus.OK);
    }

    /**
     * Avanza o actualiza el estado de un pago concreto.
     *
     * Este endpoint permite cambiar el estado de un pago siguiendo
     * las reglas de transición definidas en el dominio:
     *
     * - CREATED → PENDING
     * - PENDING → AUTHORIZED | FAILED
     * - AUTHORIZED → SUCCESS | FAILED
     * - SUCCESS → REFUNDED
     *
     * Los estados terminales (FAILED, CANCELLED, EXPIRED, REFUNDED)
     * no permiten nuevas transiciones.
     *
     * El control de las transiciones válidas se realiza en el servicio,
     * manteniendo la lógica de negocio fuera del controlador.
     *
     * @param paymentId    ID del pago a actualizar.
     * @param targetStatus Estado al que se desea avanzar el pago.
     * @return ResponseEntity con el nuevo {@link PaymentStatus} del pago
     *         y estado HTTP 202 ACCEPTED.
     * @throws OrderException si el pago no existe o se intenta una transición
     *                        inválida.
     */
    @PostMapping("/payments/{paymentId}/nextStatus")
    public ResponseEntity<PaymentStatus> updatePaymentStatus(
            @PathVariable long paymentId,
            @RequestParam PaymentStatus targetStatus) {
        PaymentStatus newStatus = pService.nextPaymentStatus(paymentId, targetStatus);
        return new ResponseEntity<>(newStatus, HttpStatus.ACCEPTED);
    }
}
