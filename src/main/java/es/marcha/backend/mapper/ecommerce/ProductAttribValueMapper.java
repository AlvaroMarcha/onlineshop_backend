package es.marcha.backend.mapper.ecommerce;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.marcha.backend.dto.request.ecommerce.product.ProductAttribValueRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.attrib.ProductAttribValueResponseDTO;
import es.marcha.backend.model.ecommerce.product.ProductAttrib;
import es.marcha.backend.model.ecommerce.product.ProductAttribValue;

public class ProductAttribValueMapper {

    /**
     * Convierte una entidad ProductAttribValue a su DTO de respuesta.
     *
     * @param productAttribValue entidad a convertir
     * @return DTO con los datos del valor del atributo
     */
    public static ProductAttribValueResponseDTO toResponseDTO(ProductAttribValue productAttribValue) {
        return ProductAttribValueResponseDTO.builder()
                .id(productAttribValue.getId())
                .attribId(productAttribValue.getAttrib().getId())
                .value(productAttribValue.getValue())
                .label(productAttribValue.getLabel())
                .colorHex(productAttribValue.getColorHex())
                .isActive(productAttribValue.isActive())
                .sortOrder(productAttribValue.getSortOrder())
                .createdAt(productAttribValue.getCreatedAt())
                .build();
    }

    /**
     * Convierte una lista de entidades ProductAttribValue a una lista de DTOs de
     * respuesta.
     *
     * @param values lista de entidades a convertir
     * @return lista de DTOs con los datos de los valores del atributo
     */
    public static List<ProductAttribValueResponseDTO> toResponseDTOList(List<ProductAttribValue> values) {
        if (values == null)
            return new ArrayList<>();
        List<ProductAttribValueResponseDTO> dtoList = new ArrayList<>();
        for (ProductAttribValue value : values) {
            dtoList.add(toResponseDTO(value));
        }
        return dtoList;
    }

    /**
     * Convierte un DTO de solicitud en una nueva entidad ProductAttribValue
     * asociada a un atributo.
     *
     * @param dto    DTO con los datos del valor a crear
     * @param attrib atributo al que pertenece el valor
     * @return nueva entidad ProductAttribValue sin persistir
     */
    public static ProductAttribValue fromRequestDTO(ProductAttribValueRequestDTO dto, ProductAttrib attrib) {
        return ProductAttribValue.builder()
                .attrib(attrib)
                .value(dto.getLabel().toLowerCase())
                .label(dto.getLabel())
                .colorHex(dto.getColorHex())
                .sortOrder(dto.getSortOrder())
                .isActive(dto.isActive())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
