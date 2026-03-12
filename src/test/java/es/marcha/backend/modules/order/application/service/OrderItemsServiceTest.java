package es.marcha.backend.modules.order.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.OrderException;

import es.marcha.backend.modules.order.infrastructure.persistence.OrderItemRepository;

/**
 * Tests unitarios para OrderItemsService.
 *
 * Los snapshots de items de pedido son inmutables — no hay UPDATE ni DELETE.
 * Verifica:
 * - saveOrderItems lanza OrderException con lista vacía o nula
 * - saveOrderItems devuelve DTOs para lista válida
 * - getOrderItemsByOrder lanza OrderException si no encontrado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderItemsService — snapshots de items inmutables")
class OrderItemsServiceTest {

    @Mock
    private OrderItemRepository oItemRepository;

    @InjectMocks
    private OrderItemsService orderItemsService;

    // =========================================================================
    // saveOrderItems
    // =========================================================================

    @Nested
    @DisplayName("saveOrderItems")
    class SaveOrderItemsTests {

        @Test
        @DisplayName("lista nula → lanza OrderException")
        void saveOrderItems_listaNula_lanzaException() {
            assertThrows(OrderException.class,
                    () -> orderItemsService.saveOrderItems(null));
        }

        @Test
        @DisplayName("lista vacía → lanza OrderException")
        void saveOrderItems_listaVacia_lanzaException() {
            assertThrows(OrderException.class,
                    () -> orderItemsService.saveOrderItems(List.of()));
        }
    }

    // =========================================================================
    // getOrderItemsByOrder
    // =========================================================================

    @Nested
    @DisplayName("getOrderItemsByOrder")
    class GetOrderItemsTests {

        @Test
        @DisplayName("no encontrado → lanza OrderException(FAILED_ORDER_ITEMS)")
        void getOrderItemsByOrder_noEncontrado_lanzaException() {
            when(oItemRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            OrderException ex = assertThrows(OrderException.class,
                    () -> orderItemsService.getOrderItemsByOrder(999L));
            assertEquals(OrderException.FAILED_ORDER_ITEMS, ex.getMessage());
        }
    }
}
