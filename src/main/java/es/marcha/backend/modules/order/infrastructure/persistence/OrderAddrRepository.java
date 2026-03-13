package es.marcha.backend.modules.order.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.order.domain.model.OrderAddresses;

@Repository
public interface OrderAddrRepository extends JpaRepository<OrderAddresses, Long> {

    Optional<OrderAddresses> findByOrderId(long id);

}
