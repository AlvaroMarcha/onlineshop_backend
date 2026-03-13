package es.marcha.backend.modules.catalog.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.application.dto.request.inventory.MovementRequestDTO;
import es.marcha.backend.modules.catalog.domain.enums.MovementType;
import es.marcha.backend.modules.catalog.domain.model.Inventory;
import es.marcha.backend.modules.catalog.domain.model.Movement;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.infrastructure.persistence.InventoryRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.MovementRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;

/**
 * Tests unitarios para InventoryService.
 *
 * Verifica:
 * - getInventoryByProductId lanza ProductException si no tiene inventario
 * - applyMovement aumenta el stock con tipo IN
 * - applyMovement reduce el stock con tipo OUT
 * - applyMovement con stock insuficiente lanza
 * ProductException(INSUFFICIENT_STOCK)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService — movimientos de stock")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    // =========================================================================
    // getInventoryByProductId
    // =========================================================================

    @Nested
    @DisplayName("getInventoryByProductId")
    class GetInventoryTests {

        @Test
        @DisplayName("sin inventario registrado → lanza ProductException")
        void getInventory_noExiste_lanzaException() {
            when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
            assertThrows(ProductException.class, () -> inventoryService.getInventoryByProductId(1L));
        }

        @Test
        @DisplayName("con inventario → devuelve DTO")
        void getInventory_existe_devuelveDTO() {
            Product product = new Product();
            product.setId(1L);
            Inventory inventory = buildInventory(product, 10);
            when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
            assertDoesNotThrow(() -> inventoryService.getInventoryByProductId(1L));
        }
    }

    // =========================================================================
    // applyMovement
    // =========================================================================

    @Nested
    @DisplayName("applyMovement")
    class ApplyMovementTests {

        @Test
        @DisplayName("movimiento IN → incrementa el stock")
        void applyMovement_tipoIN_incrementaStock() {
            Product product = buildProduct(1L);
            Inventory inventory = buildInventory(product, 5);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
            ArgumentCaptor<Movement> movCaptor = ArgumentCaptor.forClass(Movement.class);
            when(movementRepository.save(movCaptor.capture())).thenAnswer(inv -> movCaptor.getValue());
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

            MovementRequestDTO dto = new MovementRequestDTO();
            dto.setQuantity(3);
            dto.setMovementType(MovementType.IN);
            dto.setNotes("Reposición");

            inventoryService.applyMovement(1L, dto);

            assertEquals(8, product.getStock()); // 5 + 3
        }

        @Test
        @DisplayName("movimiento OUT con stock suficiente → reduce el stock")
        void applyMovement_tipoOUT_stockSuficiente_reduceStock() {
            Product product = buildProduct(1L);
            Inventory inventory = buildInventory(product, 10);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
            ArgumentCaptor<Movement> movCaptor = ArgumentCaptor.forClass(Movement.class);
            when(movementRepository.save(movCaptor.capture())).thenAnswer(inv -> movCaptor.getValue());
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

            MovementRequestDTO dto = new MovementRequestDTO();
            dto.setQuantity(4);
            dto.setMovementType(MovementType.OUT);
            dto.setNotes("Venta");

            inventoryService.applyMovement(1L, dto);

            assertEquals(6, product.getStock()); // 10 - 4
        }

        @Test
        @DisplayName("movimiento OUT con stock insuficiente → lanza ProductException(INSUFFICIENT_STOCK)")
        void applyMovement_tipoOUT_stockInsuficiente_lanzaException() {
            Product product = buildProduct(1L);
            Inventory inventory = buildInventory(product, 2); // solo 2 en stock

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

            MovementRequestDTO dto = new MovementRequestDTO();
            dto.setQuantity(5); // quiero 5, no hay suficiente
            dto.setMovementType(MovementType.OUT);

            ProductException ex = assertThrows(ProductException.class,
                    () -> inventoryService.applyMovement(1L, dto));
            assertEquals(ProductException.INSUFFICIENT_STOCK, ex.getMessage());
        }

        @Test
        @DisplayName("producto no encontrado → lanza ProductException")
        void applyMovement_productoNoExiste_lanzaException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            MovementRequestDTO dto = new MovementRequestDTO();
            dto.setQuantity(1);
            dto.setMovementType(MovementType.IN);

            assertThrows(ProductException.class, () -> inventoryService.applyMovement(99L, dto));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Product buildProduct(long id) {
        Product p = new Product();
        p.setId(id);
        p.setName("Test");
        return p;
    }

    private Inventory buildInventory(Product product, int stock) {
        product.setStock(stock);
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setProduct(product);
        inv.setQuantity(stock);
        return inv;
    }
}
