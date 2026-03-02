package es.marcha.backend.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.marcha.backend.model.order.OrderItems;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {

    // Recupera todos los items asociados a una orden concreta
    List<OrderItems> findByOrderId(long orderId);

}
