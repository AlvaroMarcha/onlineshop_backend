package es.marcha.backend.services.order;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.order.PaymentResponseDTO;
import es.marcha.backend.core.error.exception.OrderException;
import es.marcha.backend.mapper.order.PaymentMapper;
import es.marcha.backend.core.shared.domain.enums.OrderStatus;
import es.marcha.backend.core.shared.domain.enums.PaymentStatus;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.Payment;
import es.marcha.backend.repository.order.PaymentRepository;
import jakarta.transaction.Transactional;

@Service
public class PaymentService {
    // Attribs
    @Autowired
    private PaymentRepository pRepository;
    @Autowired
    private OrderService oService;

    /**
     * Crea un nuevo pago asociado a una orden y actualiza el total de la orden.
     *
     * Pasos realizados:
     * 1. Obtiene la entidad Order asociada al Payment a partir de su ID.
     * 2. Comprueba que no exista otro pago con el mismo transactionId. Si existe,
     * lanza una excepción {@link OrderException} con código DUPLICATE_TRANSACTION.
     * 3. Inicializa el Payment:
     * - Establece el estado inicial como {@link PaymentStatus#CREATED}.
     * - Registra la fecha de creación actual.
     * - Asocia el Payment a la Order obtenida.
     * 4. Actualiza el total de la Order sumando el amount del nuevo pago y guarda
     * la orden.
     * 5. Persiste el Payment en la base de datos y devuelve su representación como
     * DTO
     * {@link PaymentResponseDTO} para exponerla al front.
     *
     * Nota:
     * - Este método mantiene la consistencia entre la orden y sus pagos.
     * - No permite duplicados de transactionId para evitar conflictos de pagos.
     *
     * @param payment El pago a crear. Debe contener al menos el orderId y el
     *                transactionId.
     * @return {@link PaymentResponseDTO} DTO del pago recién creado.
     * @throws OrderException Si ya existe un pago con el mismo transactionId.
     */
    @Transactional
    public PaymentResponseDTO savePayment(Payment payment) {
        Order order = oService.getOrderByIdHandler(payment.getOrder().getId());

        if (pRepository.existsByTransactionId(payment.getTransactionId())) {
            throw new OrderException(OrderException.DUPLICATE_TRANSACTION);
        }

        // Inicializar el pago
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setOrder(order);

        return PaymentMapper.toPaymentDTO(pRepository.save(payment));
    }

    /**
     * Obtiene el último pago válido de una orden por su ID.
     * 
     * Se consideran pagos válidos aquellos cuyo estado es:
     * AUTHORIZED, PENDING, SUCCESS, REFUNDED, CANCELLED o FAILED.
     * 
     * Este método filtra los pagos por esos estados y devuelve
     * el más reciente según la fecha de creación.
     * 
     * @param id ID de la orden de la cual se quieren obtener los pagos.
     * @return PaymentResponseDTO que representa el último pago válido.
     * @throws OrderException si no existen pagos válidos para la orden.
     */
    public PaymentResponseDTO getLastValidPaymentByOrderId(long id) {
        EnumSet<PaymentStatus> validStatuses = EnumSet.of(
                PaymentStatus.CREATED,
                PaymentStatus.AUTHORIZED,
                PaymentStatus.PENDING,
                PaymentStatus.SUCCESS,
                PaymentStatus.REFUNDED,
                PaymentStatus.CANCELLED,
                PaymentStatus.FAILED);

        List<Payment> payments = pRepository.findAllByOrderId(id);
        List<Payment> validPayments = payments.stream()
                .filter(p -> validStatuses.contains(p.getStatus()))
                .toList();

        Payment lastPayment = validPayments.stream()
                .max(Comparator.comparing(Payment::getCreatedAt))
                .orElseThrow(() -> new OrderException(OrderException.NOT_VALID_PAYMENT));

        return PaymentMapper.toPaymentDTO(lastPayment);
    }

    /**
     * Avanza o actualiza el estado de un pago a un estado permitido.
     * 
     * Reglas de transición:
     * - CREATED → PENDING
     * - PENDING → AUTHORIZED o FAILED
     * - AUTHORIZED → SUCCESS o FAILED
     * - SUCCESS → REFUNDED
     * - FAILED, CANCELLED, EXPIRED, REFUNDED son estados terminales
     * y no permiten cambios.
     * 
     * @param paymentId    ID del pago a actualizar.
     * @param targetStatus Estado al que se desea avanzar el pago.
     * @return PaymentStatus estado final del pago después de la actualización.
     * @throws OrderException si el pago no existe o si se intenta cambiar
     *                        un estado terminal.
     */
    @Transactional
    public PaymentStatus nextPaymentStatus(long paymentId, PaymentStatus targetStatus) {
        Payment payment = pRepository.findById(paymentId)
                .orElseThrow(() -> new OrderException());

        PaymentStatus currentStatus = payment.getStatus();

        switch (currentStatus) {
            // Error 4: lanzar excepción si la transición no es válida
            case CREATED -> {
                if (targetStatus != PaymentStatus.PENDING)
                    throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
                currentStatus = PaymentStatus.PENDING;
            }
            case PENDING -> {
                if (targetStatus != PaymentStatus.AUTHORIZED && targetStatus != PaymentStatus.FAILED)
                    throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
                currentStatus = targetStatus;
            }
            case AUTHORIZED -> {
                if (targetStatus != PaymentStatus.SUCCESS && targetStatus != PaymentStatus.FAILED)
                    throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
                currentStatus = targetStatus;
            }
            case SUCCESS -> {
                if (targetStatus != PaymentStatus.REFUNDED)
                    throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
                currentStatus = PaymentStatus.REFUNDED;
            }
            // Error 5: eliminar llamadas inútiles a cancelPayment/refundPayment
            case FAILED, EXPIRED, CANCELLED, REFUNDED -> {
                throw new OrderException(
                        OrderException.TERMINAL_STATUS_PAYMENT + ": " + currentStatus);
            }
        }

        payment.setStatus(currentStatus);
        pRepository.save(payment);

        updateOrderStatusFromPayments(payment.getOrder());

        return payment.getStatus();
    }

    /**
     * Actualiza el estado de una orden en función del estado de sus pagos
     * asociados.
     *
     * Este método centraliza la sincronización entre el ciclo de vida de los pagos
     * y el estado global de la orden. La orden NO decide su estado por sí sola,
     * sino que refleja la realidad financiera de sus pagos.
     *
     * Reglas de negocio aplicadas:
     *
     * 1. Si existe al menos un pago SUCCESS y posteriormente un pago REFUNDED,
     * la orden se considera {@link OrderStatus#RETURNED}.
     *
     * 2. Si existe al menos un pago SUCCESS (y ninguno REFUNDED),
     * la orden se marca como {@link OrderStatus#PAID}.
     *
     * 3. Si TODOS los pagos están en estado FAILED o CANCELLED,
     * la orden se marca como {@link OrderStatus#CANCELLED}.
     *
     * 4. Si existe algún pago en progreso (CREATED, PENDING o AUTHORIZED),
     * la orden se considera {@link OrderStatus#PROCESSING}.
     *
     * 5. En cualquier otro caso no contemplado explícitamente,
     * la orden se marca como {@link OrderStatus#CANCELLED}
     * como estado seguro por defecto.
     *
     * Detalles importantes:
     * - Se analizan TODOS los pagos de la orden.
     * - Se usan operaciones de tipo {@code anyMatch} y {@code allMatch}
     * para expresar reglas de negocio de forma declarativa.
     * - La orden siempre se persiste tras recalcular su estado.
     *
     * @param order La orden cuyo estado debe recalcularse en base a sus pagos.
     */
    private void updateOrderStatusFromPayments(Order order) {
        List<Payment> payments = pRepository.findAllByOrderId(order.getId());

        boolean hasSuccess = payments.stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS);

        boolean hasRefunded = payments.stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.REFUNDED);

        boolean hasInProgress = payments.stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.CREATED
                        || p.getStatus() == PaymentStatus.PENDING
                        || p.getStatus() == PaymentStatus.AUTHORIZED);

        boolean allFailedOrCancelled = payments.stream()
                .allMatch(p -> p.getStatus() == PaymentStatus.FAILED ||
                        p.getStatus() == PaymentStatus.CANCELLED);

        if (hasSuccess && hasRefunded) {
            order.setStatus(OrderStatus.RETURNED);
        } else if (hasSuccess) {
            order.setStatus(OrderStatus.PAID);
        } else if (allFailedOrCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
        } else if (hasInProgress) {
            order.setStatus(OrderStatus.PROCESSING);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
        }

        oService.saveOrder(order);
    }

    /**
     * Cancela un pago existente y revierte su impacto en el total de la orden
     * asociada.
     *
     * <p>
     * Este método es {@link Transactional}, por lo que todas las operaciones se
     * ejecutan
     * dentro de una única transacción. Si ocurre un error durante el proceso, no se
     * persisten cambios en la base de datos.
     * </p>
     *
     * <p>
     * Comportamiento:
     * <ul>
     * <li>Si el pago no existe, lanza una {@link OrderException}.</li>
     * <li>Si el pago ya está en estado {@code CANCELLED}, el método es idempotente:
     * no modifica la orden ni el pago y devuelve el estado actual.</li>
     * <li>Si el pago no está cancelado, se resta su {@code amount} del
     * {@code totalAmount} de la orden asociada.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Este enfoque evita dobles restas en escenarios de reintentos, llamadas
     * duplicadas
     * o cancelaciones repetidas, garantizando consistencia en el total de la orden.
     * </p>
     *
     * @param paymentId identificador del pago a cancelar
     * @return {@link PaymentResponseDTO} con el estado actualizado del pago
     * @throws OrderException si el pago no existe
     */
    @Transactional
    public PaymentResponseDTO cancelPayment(long paymentId) {
        Payment payment = pRepository.findById(paymentId).orElseThrow(() -> new OrderException());

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return PaymentMapper.toPaymentDTO(payment);
        }

        EnumSet<PaymentStatus> cancellableStatuses = EnumSet.of(
                PaymentStatus.CREATED,
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED);

        if (!cancellableStatuses.contains(payment.getStatus())) {
            throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        updateOrderStatusFromPayments(payment.getOrder());
        return PaymentMapper.toPaymentDTO(pRepository.save(payment));
    }

    /**
     * Realiza el reembolso de un pago confirmado y revierte su impacto en el total
     * de la orden asociada.
     *
     * <p>
     * Este método es {@link Transactional}, por lo que todas las operaciones se
     * ejecutan en una única transacción. Si ocurre un error durante el proceso, no
     * se persisten cambios en la base de datos.
     * </p>
     *
     * <p>
     * Comportamiento:
     * <ul>
     * <li>Si el pago no existe, lanza una {@link OrderException}.</li>
     * <li>Si el pago ya está en estado {@code REFUNDED}, el método es idempotente:
     * no modifica la orden ni el pago y devuelve el estado actual.</li>
     * <li>Si el pago no está reembolsado, se resta su {@code amount} del
     * {@code totalAmount} de la orden asociada y se marca como
     * {@code REFUNDED}.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Esta lógica asegura que el total de la orden refleje correctamente los pagos
     * que han sido reembolsados, evitando dobles restas y manteniendo la
     * consistencia
     * del sistema.
     * </p>
     *
     * @param paymentId identificador del pago a reembolsar
     * @return {@link PaymentResponseDTO} con el estado actualizado del pago
     * @throws OrderException si el pago no existe
     */
    @Transactional
    public PaymentResponseDTO refundPayment(long paymentId) {
        Payment payment = pRepository.findById(paymentId)
                .orElseThrow(() -> new OrderException());

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return PaymentMapper.toPaymentDTO(payment);
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new OrderException(OrderException.INVALID_STATUS_TRANSITION);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        updateOrderStatusFromPayments(payment.getOrder());
        return PaymentMapper.toPaymentDTO(pRepository.save(payment));
    }

}
