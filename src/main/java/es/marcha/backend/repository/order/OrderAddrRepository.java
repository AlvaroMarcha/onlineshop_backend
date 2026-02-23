package es.marcha.backend.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.order.OrderAddresses;

@Repository
public interface OrderAddrRepository extends JpaRepository<OrderAddresses, Long> {

}
