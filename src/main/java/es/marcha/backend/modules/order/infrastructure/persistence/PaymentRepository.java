package es.marcha.backend.modules.order.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.order.domain.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderId(Long orderId);

    boolean existsByTransactionId(String transactionId);

    Optional<Payment> findByTransactionId(String transactionId);

}
