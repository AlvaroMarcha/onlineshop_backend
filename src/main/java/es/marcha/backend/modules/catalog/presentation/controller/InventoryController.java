package es.marcha.backend.modules.catalog.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.modules.catalog.application.dto.request.inventory.InventoryUpdateRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.request.inventory.MovementRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.response.inventory.InventoryResponseDTO;
import es.marcha.backend.modules.catalog.application.dto.response.inventory.MovementResponseDTO;
import es.marcha.backend.modules.catalog.application.service.InventoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // ─── Inventario ─────────────────────────────────────────────────────────

    /**
     * Obtiene el inventario de un producto concreto.
     *
     * @param productId ID del producto
     * @return {@link InventoryResponseDTO} con los datos del inventario y HTTP 200
     *         OK.
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponseDTO> getInventoryByProduct(@PathVariable long productId) {
        return new ResponseEntity<>(inventoryService.getInventoryByProductId(productId), HttpStatus.OK);
    }

    /**
     * Actualiza los metadatos del inventario de un producto: umbrales de stock,
     * stock en camino y stock dañado.
     * No modifica el stock disponible; para eso se usa POST /inventory/movements.
     *
     * @param productId ID del producto
     * @param dto       datos de actualización del inventario
     * @return {@link InventoryResponseDTO} actualizado y HTTP 200 OK.
     */
    @PutMapping("/products/{productId}")
    public ResponseEntity<InventoryResponseDTO> updateInventory(
            @PathVariable long productId,
            @RequestBody InventoryUpdateRequestDTO dto) {
        return new ResponseEntity<>(inventoryService.updateInventoryMetadata(productId, dto), HttpStatus.OK);
    }

    // ─── Movimientos ─────────────────────────────────────────────────────────

    /**
     * Registra un movimiento manual de stock para un producto.
     * Dependiendo del tipo de movimiento, suma o resta stock al producto.
     * También actualiza el inventario y deja trazabilidad en la tabla de
     * movimientos.
     *
     * <p>
     * Tipos que <b>suman</b> stock: {@code IN}, {@code RESTOCK},
     * {@code PURCHASE}, {@code RETURN}, {@code ADJUSTMENT}.
     * </p>
     * <p>
     * Tipos que <b>restan</b> stock: {@code OUT}, {@code SALE}.
     * </p>
     *
     * @param productId ID del producto al que se aplica el movimiento
     * @param dto       datos del movimiento (cantidad, tipo, notas)
     * @return {@link MovementResponseDTO} del movimiento creado y HTTP 201 CREATED.
     */
    @PostMapping("/products/{productId}/movements")
    public ResponseEntity<MovementResponseDTO> applyMovement(
            @PathVariable long productId,
            @Valid @RequestBody MovementRequestDTO dto) {
        return new ResponseEntity<>(inventoryService.applyMovement(productId, dto), HttpStatus.CREATED);
    }

    /**
     * Lista los movimientos de un producto con paginación.
     *
     * @param productId ID del producto
     * @param page      número de página (0-based, por defecto 0)
     * @param size      tamaño de página (por defecto 20)
     * @return página de {@link MovementResponseDTO} y HTTP 200 OK.
     */
    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<Page<MovementResponseDTO>> getMovementsByProduct(
            @PathVariable long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MovementResponseDTO> movements = inventoryService.getMovementsByProduct(
                productId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return new ResponseEntity<>(movements, HttpStatus.OK);
    }

    /**
     * Lista todos los movimientos del sistema con paginación para el panel de
     * administración.
     *
     * @param page número de página (0-based, por defecto 0)
     * @param size tamaño de página (por defecto 20)
     * @return página de {@link MovementResponseDTO} y HTTP 200 OK.
     */
    @GetMapping("/movements")
    public ResponseEntity<Page<MovementResponseDTO>> getAllMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MovementResponseDTO> movements = inventoryService.getAllMovements(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return new ResponseEntity<>(movements, HttpStatus.OK);
    }
}
