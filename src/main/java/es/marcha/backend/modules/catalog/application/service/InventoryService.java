package es.marcha.backend.modules.catalog.application.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.catalog.application.dto.request.inventory.InventoryUpdateRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.request.inventory.MovementRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.response.inventory.InventoryResponseDTO;
import es.marcha.backend.modules.catalog.application.dto.response.inventory.MovementResponseDTO;
import es.marcha.backend.modules.catalog.application.mapper.InventoryMapper;
import es.marcha.backend.modules.catalog.application.mapper.MovementMapper;
import es.marcha.backend.modules.catalog.domain.enums.MovementType;
import es.marcha.backend.modules.catalog.domain.model.Inventory;
import es.marcha.backend.modules.catalog.domain.model.Movement;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.infrastructure.persistence.InventoryRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.MovementRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.core.error.exception.ProductException;
import jakarta.transaction.Transactional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private ProductRepository productRepository;

    // ─── Consulta ──────────────────────────────────────────────────────────────

    /**
     * Obtiene el inventario de un producto por su ID.
     *
     * @param productId ID del producto
     * @return {@link InventoryResponseDTO} con los datos del inventario
     * @throws ProductException si el producto no tiene inventario registrado
     */
    public InventoryResponseDTO getInventoryByProductId(long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));
        return InventoryMapper.toInventoryDTO(inventory);
    }

    // ─── Actualización de metadatos ─────────────────────────────────────────

    /**
     * Actualiza los campos de configuración del inventario de un producto:
     * minStock, maxStock, incomingStock y damagedStock.
     * No modifica el stock real (para eso se usa {@link #applyMovement}).
     *
     * @param productId ID del producto
     * @param dto       datos de actualización
     * @return {@link InventoryResponseDTO} actualizado
     * @throws ProductException si el producto no tiene inventario registrado
     */
    @Transactional
    public InventoryResponseDTO updateInventoryMetadata(long productId, InventoryUpdateRequestDTO dto) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

        inventory.setMinStock(dto.getMinStock());
        inventory.setMaxStock(dto.getMaxStock());
        inventory.setIncomingStock(dto.getIncomingStock());
        inventory.setDamagedStock(dto.getDamagedStock());
        inventory.setUpdatedAt(LocalDateTime.now());

        return InventoryMapper.toInventoryDTO(inventoryRepository.save(inventory));
    }

    // ─── Movimientos ────────────────────────────────────────────────────────

    /**
     * Aplica un movimiento manual de stock para un producto.
     *
     * <p>
     * Tipos que <b>suman</b> al stock: {@code IN}, {@code RESTOCK},
     * {@code PURCHASE}, {@code RETURN}.
     * </p>
     * <p>
     * Tipos que <b>restan</b> del stock: {@code OUT}, {@code SALE}.
     * </p>
     * <p>
     * {@code ADJUSTMENT} siempre suma. Para reducir stock manualmente,
     * usar tipo {@code OUT}.
     * </p>
     *
     * @param productId ID del producto
     * @param dto       datos del movimiento (cantidad, tipo, notas)
     * @return {@link MovementResponseDTO} del movimiento registrado
     * @throws ProductException si el producto no existe, no tiene inventario
     *                          o el stock resultante sería negativo
     */
    @Transactional
    public MovementResponseDTO applyMovement(long productId, MovementRequestDTO dto) {
        // Obtener el producto y su inventario
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ProductException.DEFAULT));

        int previousStock = product.getStock();
        int newStock = calculateNewStock(previousStock, dto.getQuantity(), dto.getMovementType());

        // Validar que el stock no quede negativo
        if (newStock < 0)
            throw new ProductException(ProductException.INSUFFICIENT_STOCK);

        // Actualizar stock en el producto
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // Sincronizar quantity en el inventario
        inventory.setQuantity(newStock);
        inventory.setUpdatedAt(LocalDateTime.now());
        // Si el movimiento es de entrada, actualizar fecha de último restock
        if (isInboundMovement(dto.getMovementType())) {
            inventory.setLastRestockDate(LocalDateTime.now());
        }
        inventoryRepository.save(inventory);

        // Registrar el movimiento
        Movement movement = Movement.builder()
                .product(product)
                .quantity(dto.getQuantity())
                .previousQuantity(previousStock)
                .newQuantity(newStock)
                .movementType(dto.getMovementType())
                .notes(dto.getNotes())
                .createdBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "ADMIN")
                .createdAt(LocalDateTime.now())
                .build();

        return MovementMapper.toMovementDTO(movementRepository.save(movement));
    }

    /**
     * Registra un movimiento de stock de forma interna (usado por otros servicios
     * como {@code OrderService} al crear un pedido o {@code ProductService} al
     * actualizar el stock manualmente).
     *
     * <p>
     * A diferencia de {@link #applyMovement}, este método <b>no modifica</b>
     * el stock del producto ni el inventario — asume que ya fueron actualizados
     * por el servicio llamante. Solo persiste el registro del movimiento.
     * </p>
     *
     * @param product          producto al que pertenece el movimiento
     * @param quantity         cantidad del movimiento
     * @param previousQuantity stock antes del movimiento
     * @param newQuantity      stock después del movimiento
     * @param movementType     tipo de movimiento
     * @param notes            notas opcionales
     * @param createdBy        quién generó el movimiento
     */
    @Transactional
    public void recordMovementInternal(Product product, int quantity, int previousQuantity,
            int newQuantity, MovementType movementType, String notes, String createdBy) {

        // Sincronizar quantity en el inventario
        inventoryRepository.findByProductId(product.getId()).ifPresent(inv -> {
            inv.setQuantity(newQuantity);
            inv.setUpdatedAt(LocalDateTime.now());
            if (isInboundMovement(movementType)) {
                inv.setLastRestockDate(LocalDateTime.now());
            }
            inventoryRepository.save(inv);
        });

        // Persistir el movimiento
        Movement movement = Movement.builder()
                .product(product)
                .quantity(quantity)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .movementType(movementType)
                .notes(notes)
                .createdBy(createdBy != null ? createdBy : "SYSTEM")
                .createdAt(LocalDateTime.now())
                .build();

        movementRepository.save(movement);
    }

    /**
     * Obtiene los movimientos de un producto con paginación.
     *
     * @param productId ID del producto
     * @param pageable  configuración de paginación
     * @return página de {@link MovementResponseDTO}
     */
    public Page<MovementResponseDTO> getMovementsByProduct(long productId, Pageable pageable) {
        return movementRepository.findAllByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(MovementMapper::toMovementDTO);
    }

    /**
     * Obtiene todos los movimientos del sistema con paginación para el panel de
     * administración.
     *
     * @param pageable configuración de paginación
     * @return página de {@link MovementResponseDTO}
     */
    public Page<MovementResponseDTO> getAllMovements(Pageable pageable) {
        return movementRepository.findAll(pageable)
                .map(MovementMapper::toMovementDTO);
    }

    // ─── Utilidades internas ────────────────────────────────────────────────

    /**
     * Calcula el nuevo stock en función de la cantidad y el tipo de movimiento.
     * Los tipos de entrada suman; los de salida restan.
     *
     * @param current      stock actual
     * @param quantity     cantidad del movimiento (siempre positiva)
     * @param movementType tipo de movimiento
     * @return nuevo stock calculado
     */
    private int calculateNewStock(int current, int quantity, MovementType movementType) {
        return switch (movementType) {
            case IN, RESTOCK, PURCHASE, RETURN, ADJUSTMENT -> current + quantity;
            case OUT, SALE -> current - quantity;
        };
    }

    /**
     * Indica si un tipo de movimiento es de entrada (añade stock).
     *
     * @param movementType tipo de movimiento
     * @return {@code true} si es un movimiento de entrada
     */
    private boolean isInboundMovement(MovementType movementType) {
        return switch (movementType) {
            case IN, RESTOCK, PURCHASE -> true;
            default -> false;
        };
    }
}
