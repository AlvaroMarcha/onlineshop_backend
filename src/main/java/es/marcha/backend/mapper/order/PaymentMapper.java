package es.marcha.backend.mapper.order;

import java.time.LocalDateTime;

import es.marcha.backend.dto.response.order.PaymentResponseDTO;
import es.marcha.backend.model.order.Order;
import es.marcha.backend.model.order.Payment;

public class PaymentMapper {
    public static PaymentResponseDTO toPaymentDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .provider(payment.getProvider())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public static Payment toPayment(PaymentResponseDTO dto, Order order) {
        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setOrder(order);
        payment.setStatus(dto.getStatus());
        payment.setAmount(dto.getAmount());
        payment.setProvider(dto.getProvider());
        payment.setTransactionId(dto.getTransactionId());
        payment.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        return payment;
    }
}
