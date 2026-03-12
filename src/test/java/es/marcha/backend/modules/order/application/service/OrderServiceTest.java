package es.marcha.backend.modules.order.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import es.marcha.backend.core.error.exception.OrderException;
import es.marcha.backend.core.user.application.service.UserService;
import es.marcha.backend.modules.coupon.application.service.CouponService;
import es.marcha.backend.modules.catalog.application.service.InventoryService;
import es.marcha.backend.modules.cart.application.service.CartService;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.invoice.application.service.InvoiceService;
import es.marcha.backend.modules.notification.application.service.UserEmailNotificationService;
import es.marcha.backend.modules.order.domain.model.Order;
import es.marcha.backend.modules.order.infrastructure.persistence.OrderRepository;
import es.marcha.backend.core.user.infrastructure.persistence.AddressRepository;

/**
 * Tests unitarios para OrderService.
 *
 * Regla de negocio: el totalAmount se calcula en el backend desde precios
 * reales. El frontend solo envía productId + quantity.
 *
 * Verifica:
 * - getOrderByIdHandler lanza OrderException si la orden no existe
 * - saveNewOrder lanza OrderException si el usuario no tiene dirección
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — gestión de pedidos")
class OrderServiceTest {

    @Mock
    private OrderRepository oRepository;

    @Mock
    private OrderAddressService oAddrService;

    @Mock
    private OrderItemsService oItemsService;

    @Mock
    private UserService uService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserEmailNotificationService userEmailNotificationService;

    @Mock
    private CartService cartService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CouponService couponService;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private OrderService orderService;

    // =========================================================================
    // getOrderByIdHandler
    // =========================================================================

    @Nested
    @DisplayName("getOrderByIdHandler")
    class GetOrderByIdHandlerTests {

        @Test
        @DisplayName("orden no encontrada → lanza OrderException")
        void getOrderByIdHandler_noEncontrada_lanzaException() {
            when(oRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(OrderException.class,
                    () -> orderService.getOrderByIdHandler(99L));
        }

        @Test
        @DisplayName("orden encontrada → devuelve la entidad")
        void getOrderByIdHandler_encontrada_devuelveOrden() {
            Order order = new Order();
            order.setId(1L);
            when(oRepository.findById(1L)).thenReturn(Optional.of(order));

            Order result = orderService.getOrderByIdHandler(1L);
            assertNotNull(result);
            assertEquals(1L, result.getId());
        }
    }

    // =========================================================================
    // saveNewOrder — validaciones previas
    // =========================================================================

    @Nested
    @DisplayName("saveNewOrder — validaciones")
    class SaveNewOrderTests {

        @Test
        @DisplayName("usuario sin direcciones → lanza OrderException(USER_ADDRESS_LENGHT_0)")
        void saveNewOrder_sinDirecciones_lanzaException() {
            es.marcha.backend.core.user.domain.model.User user = new es.marcha.backend.core.user.domain.model.User();
            user.setId(1L);
            user.setAddresses(null); // sin direcciones

            when(uService.getUserByIdForHandler(1L)).thenReturn(user);

            es.marcha.backend.modules.order.application.dto.request.OrderRequestDTO request = new es.marcha.backend.modules.order.application.dto.request.OrderRequestDTO();
            request.setUserId(1L);

            OrderException ex = assertThrows(OrderException.class,
                    () -> orderService.saveNewOrder(request));
            assertEquals(OrderException.USER_ADDRESS_LENGHT_0, ex.getMessage());
        }
    }
}
