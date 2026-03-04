package es.marcha.backend.services.ecommerce;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.ecommerce.product.ProductAttribRequestDTO;
import es.marcha.backend.dto.request.ecommerce.product.ProductAttribValueRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.attrib.ProductAttribResponseDTO;
import es.marcha.backend.dto.response.ecommerce.product.attrib.ProductAttribValueResponseDTO;
import es.marcha.backend.core.error.exception.ProductAttribException;
import es.marcha.backend.mapper.ecommerce.ProductAttribMapper;
import es.marcha.backend.mapper.ecommerce.ProductAttribValueMapper;
import es.marcha.backend.model.ecommerce.product.ProductAttrib;
import es.marcha.backend.model.ecommerce.product.ProductAttribValue;
import es.marcha.backend.repository.ecommerce.ProductAttribRepository;
import es.marcha.backend.repository.ecommerce.ProductAttribValueRepository;
import es.marcha.backend.core.shared.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class ProductAttribService {

    public static final String ATTRIB_DELETED = "ATTRIB WAS DELETED";
    public static final String ATTRIB_VALUE_DELETED = "ATTRIB VALUE WAS DELETED";

    @Autowired
    private ProductAttribRepository attribRepository;

    @Autowired
    private ProductAttribValueRepository attribValueRepository;

    // ─── ProductAttrib
    // ────────────────────────────────────────────────────────────

    /**
     * Obtiene un atributo por su ID.
     *
     * @param id identificador del atributo
     * @return DTO con los datos del atributo y sus valores
     * @throws ProductAttribException si el atributo no existe
     */
    public ProductAttribResponseDTO getAttribById(long id) {
        return attribRepository.findById(id)
                .map(ProductAttribMapper::toResponseDTO)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_NOT_FOUND));
    }

    /**
     * Obtiene todos los atributos registrados en el sistema.
     *
     * @return lista de DTOs con todos los atributos y sus valores
     * @throws ProductAttribException si no hay atributos registrados
     */
    public List<ProductAttribResponseDTO> getAllAttribs() {
        List<ProductAttribResponseDTO> attribs = attribRepository.findAll().stream()
                .map(ProductAttribMapper::toResponseDTO)
                .toList();

        if (attribs.isEmpty()) {
            throw new ProductAttribException(ProductAttribException.FAILED_FETCH_ATTRIBS);
        }

        return attribs;
    }

    /**
     * Crea un nuevo atributo de producto.
     * El slug se genera siempre automáticamente a partir del nombre.
     * Valida que el slug generado no esté ya en uso.
     *
     * @param dto datos del atributo a crear
     * @return DTO con los datos del atributo creado
     * @throws ProductAttribException si el slug ya existe o los datos son inválidos
     */
    public ProductAttribResponseDTO createAttrib(ProductAttribRequestDTO dto) {
        if (dto == null) {
            throw new ProductAttribException(ProductAttribException.FAILED_CREATE_ATTRIB);
        }

        String slug = ProductUtils.createSlug(dto.getName());
        if (attribRepository.existsBySlug(slug)) {
            throw new ProductAttribException(ProductAttribException.SLUG_ALREADY_EXISTS);
        }

        ProductAttrib attrib = ProductAttribMapper.fromRequestDTO(dto);
        attrib.setSlug(slug);
        return ProductAttribMapper.toResponseDTO(attribRepository.save(attrib));
    }

    /**
     * Actualiza los campos de un atributo existente.
     * El slug se regenera automáticamente a partir del nuevo nombre.
     * Valida que el slug generado no esté ya en uso por otro atributo.
     *
     * @param id  identificador del atributo a actualizar
     * @param dto datos actualizados del atributo
     * @return DTO con los datos del atributo actualizado
     * @throws ProductAttribException si el atributo no existe o el slug ya está en
     *                                uso
     */
    @Transactional
    public ProductAttribResponseDTO updateAttrib(long id, ProductAttribRequestDTO dto) {
        ProductAttrib attrib = attribRepository.findById(id)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_NOT_FOUND));

        String slug = ProductUtils.createSlug(dto.getName());
        if (!attrib.getSlug().equals(slug) && attribRepository.existsBySlug(slug)) {
            throw new ProductAttribException(ProductAttribException.SLUG_ALREADY_EXISTS);
        }

        attrib.setName(dto.getName());
        attrib.setDescription(dto.getDescription());
        attrib.setSlug(slug);
        attrib.setType(dto.getType());
        attrib.setRequired(dto.isRequired());
        attrib.setSortOrder(dto.getSortOrder());
        attrib.setUpdatedAt(LocalDateTime.now());

        return ProductAttribMapper.toResponseDTO(attribRepository.save(attrib));
    }

    /**
     * Elimina un atributo y todos sus valores asociados en cascada.
     *
     * @param id identificador del atributo a eliminar
     * @return mensaje de confirmación de eliminación
     * @throws ProductAttribException si el atributo no existe
     */
    @Transactional
    public String deleteAttrib(long id) {
        if (!attribRepository.existsById(id)) {
            throw new ProductAttribException(ProductAttribException.ATTRIB_NOT_FOUND);
        }
        attribRepository.deleteById(id);
        return ATTRIB_DELETED;
    }

    // ─── ProductAttribValue
    // ───────────────────────────────────────────────────────

    /**
     * Obtiene todos los valores definidos para un atributo concreto.
     *
     * @param attribId identificador del atributo padre
     * @return lista de DTOs con los valores del atributo
     * @throws ProductAttribException si el atributo no existe o no tiene valores
     */
    public List<ProductAttribValueResponseDTO> getAttribValues(long attribId) {
        if (!attribRepository.existsById(attribId)) {
            throw new ProductAttribException(ProductAttribException.ATTRIB_NOT_FOUND);
        }

        List<ProductAttribValueResponseDTO> values = attribValueRepository.findAllByAttribId(attribId).stream()
                .map(ProductAttribValueMapper::toResponseDTO)
                .toList();

        if (values.isEmpty()) {
            throw new ProductAttribException(ProductAttribException.FAILED_FETCH_ATTRIB_VALUES);
        }

        return values;
    }

    /**
     * Crea un nuevo valor para un atributo existente.
     *
     * @param attribId identificador del atributo al que se añade el valor
     * @param dto      datos del valor a crear
     * @return DTO con los datos del valor creado
     * @throws ProductAttribException si el atributo no existe o los datos son
     *                                inválidos
     */
    public ProductAttribValueResponseDTO createAttribValue(long attribId, ProductAttribValueRequestDTO dto) {
        if (dto == null) {
            throw new ProductAttribException(ProductAttribException.FAILED_CREATE_ATTRIB_VALUE);
        }

        ProductAttrib attrib = attribRepository.findById(attribId)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_NOT_FOUND));

        ProductAttribValue value = ProductAttribValueMapper.fromRequestDTO(dto, attrib);
        return ProductAttribValueMapper.toResponseDTO(attribValueRepository.save(value));
    }

    /**
     * Actualiza los campos de un valor de atributo existente.
     *
     * @param id  identificador del valor a actualizar
     * @param dto datos actualizados del valor
     * @return DTO con los datos del valor actualizado
     * @throws ProductAttribException si el valor no existe
     */
    @Transactional
    public ProductAttribValueResponseDTO updateAttribValue(long id, ProductAttribValueRequestDTO dto) {
        ProductAttribValue value = attribValueRepository.findById(id)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_VALUE_NOT_FOUND));

        value.setValue(dto.getLabel().toLowerCase());
        value.setLabel(dto.getLabel());
        value.setColorHex(dto.getColorHex());
        value.setSortOrder(dto.getSortOrder());
        value.setActive(dto.isActive());
        value.setUpdatedAt(LocalDateTime.now());

        return ProductAttribValueMapper.toResponseDTO(attribValueRepository.save(value));
    }

    /**
     * Elimina un valor de atributo por su ID.
     *
     * @param id identificador del valor a eliminar
     * @return mensaje de confirmación de eliminación
     * @throws ProductAttribException si el valor no existe
     */
    @Transactional
    public String deleteAttribValue(long id) {
        ProductAttribValue value = attribValueRepository.findById(id)
                .orElseThrow(() -> new ProductAttribException(ProductAttribException.ATTRIB_VALUE_NOT_FOUND));

        ProductAttrib attrib = value.getAttrib();
        attrib.getValues().remove(value);
        attribRepository.save(attrib);
        return ATTRIB_VALUE_DELETED;
    }
}
