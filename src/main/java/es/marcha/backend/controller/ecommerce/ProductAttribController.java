package es.marcha.backend.controller.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.request.ecommerce.product.ProductAttribRequestDTO;
import es.marcha.backend.dto.request.ecommerce.product.ProductAttribValueRequestDTO;
import es.marcha.backend.dto.request.ecommerce.product.ProductVariantRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.attrib.ProductAttribResponseDTO;
import es.marcha.backend.dto.response.ecommerce.product.attrib.ProductAttribValueResponseDTO;
import es.marcha.backend.dto.response.ecommerce.product.variant.ProductVariantResponseDTO;
import es.marcha.backend.services.ecommerce.ProductAttribService;
import es.marcha.backend.services.ecommerce.ProductVariantService;

@RestController
@RequestMapping("/products")
public class ProductAttribController {

    @Autowired
    private ProductAttribService attribService;

    @Autowired
    private ProductVariantService variantService;

    // ─── ProductAttrib
    // ────────────────────────────────────────────────────────────

    /**
     * Obtiene todos los atributos registrados en el sistema.
     *
     * @return {@link ResponseEntity} con la lista de
     *         {@link ProductAttribResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping("/attribs")
    public ResponseEntity<List<ProductAttribResponseDTO>> getAllAttribs() {
        return new ResponseEntity<>(attribService.getAllAttribs(), HttpStatus.OK);
    }

    /**
     * Obtiene un atributo por su ID.
     *
     * @param id identificador del atributo
     * @return {@link ResponseEntity} con el {@link ProductAttribResponseDTO}
     *         correspondiente y código HTTP 200 OK.
     */
    @GetMapping("/attribs/{id}")
    public ResponseEntity<ProductAttribResponseDTO> getAttribById(@PathVariable long id) {
        return new ResponseEntity<>(attribService.getAttribById(id), HttpStatus.OK);
    }

    /**
     * Crea un nuevo atributo de producto.
     * El slug se genera automáticamente a partir del nombre si no se proporciona.
     *
     * @param dto datos del atributo a crear
     * @return {@link ResponseEntity} con el {@link ProductAttribResponseDTO} creado
     *         y código HTTP 201 CREATED.
     */
    @PostMapping("/attribs")
    public ResponseEntity<ProductAttribResponseDTO> createAttrib(@RequestBody ProductAttribRequestDTO dto) {
        return new ResponseEntity<>(attribService.createAttrib(dto), HttpStatus.CREATED);
    }

    /**
     * Actualiza un atributo existente.
     *
     * @param id  identificador del atributo a actualizar
     * @param dto datos actualizados del atributo
     * @return {@link ResponseEntity} con el {@link ProductAttribResponseDTO}
     *         actualizado y código HTTP 200 OK.
     */
    @PutMapping("/attribs/{id}")
    public ResponseEntity<ProductAttribResponseDTO> updateAttrib(
            @PathVariable long id,
            @RequestBody ProductAttribRequestDTO dto) {
        return new ResponseEntity<>(attribService.updateAttrib(id, dto), HttpStatus.OK);
    }

    /**
     * Elimina un atributo y todos sus valores asociados en cascada.
     *
     * @param id identificador del atributo a eliminar
     * @return {@link ResponseEntity} con mensaje de confirmación y código HTTP 200
     *         OK.
     */
    @DeleteMapping("/attribs/{id}")
    public ResponseEntity<String> deleteAttrib(@PathVariable long id) {
        return new ResponseEntity<>(attribService.deleteAttrib(id), HttpStatus.OK);
    }

    // ─── ProductAttribValue
    // ───────────────────────────────────────────────────────

    /**
     * Obtiene todos los valores definidos para un atributo concreto.
     *
     * @param attribId identificador del atributo padre
     * @return {@link ResponseEntity} con la lista de
     *         {@link ProductAttribValueResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping("/attribs/{attribId}/values")
    public ResponseEntity<List<ProductAttribValueResponseDTO>> getAttribValues(@PathVariable long attribId) {
        return new ResponseEntity<>(attribService.getAttribValues(attribId), HttpStatus.OK);
    }

    /**
     * Crea un nuevo valor para un atributo existente.
     *
     * @param attribId identificador del atributo al que pertenece el valor
     * @param dto      datos del valor a crear
     * @return {@link ResponseEntity} con el {@link ProductAttribValueResponseDTO}
     *         creado y código HTTP 201 CREATED.
     */
    @PostMapping("/attribs/{attribId}/values")
    public ResponseEntity<ProductAttribValueResponseDTO> createAttribValue(
            @PathVariable long attribId,
            @RequestBody ProductAttribValueRequestDTO dto) {
        return new ResponseEntity<>(attribService.createAttribValue(attribId, dto), HttpStatus.CREATED);
    }

    /**
     * Actualiza un valor de atributo existente.
     *
     * @param id  identificador del valor a actualizar
     * @param dto datos actualizados del valor
     * @return {@link ResponseEntity} con el {@link ProductAttribValueResponseDTO}
     *         actualizado y código HTTP 200 OK.
     */
    @PutMapping("/attribs/values/{id}")
    public ResponseEntity<ProductAttribValueResponseDTO> updateAttribValue(
            @PathVariable long id,
            @RequestBody ProductAttribValueRequestDTO dto) {
        return new ResponseEntity<>(attribService.updateAttribValue(id, dto), HttpStatus.OK);
    }

    /**
     * Elimina un valor de atributo por su ID.
     *
     * @param id identificador del valor a eliminar
     * @return {@link ResponseEntity} con mensaje de confirmación y código HTTP 200
     *         OK.
     */
    @DeleteMapping("/attribs/values/{id}")
    public ResponseEntity<String> deleteAttribValue(@PathVariable long id) {
        return new ResponseEntity<>(attribService.deleteAttribValue(id), HttpStatus.OK);
    }

    // ─── ProductVariant
    // ───────────────────────────────────────────────────────────

    /**
     * Obtiene todas las variantes de un producto concreto.
     *
     * @param productId identificador del producto
     * @return {@link ResponseEntity} con la lista de
     *         {@link ProductVariantResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<ProductVariantResponseDTO>> getVariantsByProduct(@PathVariable long productId) {
        return new ResponseEntity<>(variantService.getVariantsByProduct(productId), HttpStatus.OK);
    }

    /**
     * Obtiene una variante por su ID.
     *
     * @param id identificador de la variante
     * @return {@link ResponseEntity} con el {@link ProductVariantResponseDTO} y
     *         código HTTP 200 OK.
     */
    @GetMapping("/variants/{id}")
    public ResponseEntity<ProductVariantResponseDTO> getVariantById(@PathVariable long id) {
        return new ResponseEntity<>(variantService.getVariantById(id), HttpStatus.OK);
    }

    /**
     * Crea una nueva variante para un producto, asignando los valores de atributos
     * indicados.
     *
     * @param productId identificador del producto al que pertenece la variante
     * @param dto       datos de la variante, incluyendo la lista de attribValueIds
     *                  a asignar
     * @return {@link ResponseEntity} con el {@link ProductVariantResponseDTO}
     *         creado y código HTTP 201 CREATED.
     */
    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductVariantResponseDTO> createVariant(
            @PathVariable long productId,
            @RequestBody ProductVariantRequestDTO dto) {
        return new ResponseEntity<>(variantService.createVariant(productId, dto), HttpStatus.CREATED);
    }

    /**
     * Actualiza los campos escalares de una variante (SKU, precios, stock, estado).
     *
     * @param id  identificador de la variante a actualizar
     * @param dto datos actualizados de la variante
     * @return {@link ResponseEntity} con el {@link ProductVariantResponseDTO}
     *         actualizado y código HTTP 200 OK.
     */
    @PutMapping("/variants/{id}")
    public ResponseEntity<ProductVariantResponseDTO> updateVariant(
            @PathVariable long id,
            @RequestBody ProductVariantRequestDTO dto) {
        return new ResponseEntity<>(variantService.updateVariant(id, dto), HttpStatus.OK);
    }

    /**
     * Elimina una variante y sus atributos asignados en cascada.
     *
     * @param id identificador de la variante a eliminar
     * @return {@link ResponseEntity} con mensaje de confirmación y código HTTP 200
     *         OK.
     */
    @DeleteMapping("/variants/{id}")
    public ResponseEntity<String> deleteVariant(@PathVariable long id) {
        return new ResponseEntity<>(variantService.deleteVariant(id), HttpStatus.OK);
    }

    // ─── ProductVariantAttrib
    // ─────────────────────────────────────────────────────

    /**
     * Añade un valor de atributo a una variante existente.
     *
     * @param variantId     identificador de la variante
     * @param attribValueId identificador del valor de atributo a añadir
     * @return {@link ResponseEntity} con la variante actualizada y código HTTP 200
     *         OK.
     */
    @PostMapping("/variants/{variantId}/attribs/{attribValueId}")
    public ResponseEntity<ProductVariantResponseDTO> addAttribToVariant(
            @PathVariable long variantId,
            @PathVariable long attribValueId) {
        return new ResponseEntity<>(variantService.addAttribValueToVariant(variantId, attribValueId), HttpStatus.OK);
    }

    /**
     * Elimina un valor de atributo de una variante.
     *
     * @param variantId       identificador de la variante
     * @param variantAttribId identificador del registro de atributo asignado a
     *                        eliminar
     * @return {@link ResponseEntity} con la variante actualizada y código HTTP 200
     *         OK.
     */
    @DeleteMapping("/variants/{variantId}/attribs/{variantAttribId}")
    public ResponseEntity<ProductVariantResponseDTO> removeAttribFromVariant(
            @PathVariable long variantId,
            @PathVariable long variantAttribId) {
        return new ResponseEntity<>(variantService.removeAttribValueFromVariant(variantId, variantAttribId),
                HttpStatus.OK);
    }
}
