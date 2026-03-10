package es.marcha.backend.modules.order.infrastructure.persistence;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.marcha.backend.modules.order.domain.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);

    /**
     * Obtiene todas las órdenes cuyo usuario existe, con paginación, para el panel
     * de administración.
     * Usa INNER JOIN para excluir pedidos huérfanos (usuario eliminado sin cascada)
     * y countQuery explícito para evitar que Spring Data genere una count query
     * errónea al hacer paginación.
     *
     * @param pageable Configuración de paginación
     * @return Page de órdenes con usuarios existentes
     */
    @Query(value = "SELECT DISTINCT o FROM Order o JOIN FETCH o.user", countQuery = "SELECT count(o) FROM Order o JOIN o.user")
    Page<Order> findAllWithUser(Pageable pageable);

}
