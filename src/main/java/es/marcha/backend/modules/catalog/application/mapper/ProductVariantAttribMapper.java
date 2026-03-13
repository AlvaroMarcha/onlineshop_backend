package es.marcha.backend.modules.catalog.application.mapper;

import java.util.ArrayList;
import java.util.List;

import es.marcha.backend.modules.catalog.application.dto.response.product.variant.ProductVariantAttribResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.product.ProductVariantAttrib;

public class ProductVariantAttribMapper {

    /**
     * Convierte una entidad ProductVariantAttrib a su DTO de respuesta.
     * Incluye los datos del atributo (nombre) y del valor asignado (value, label,
     * colorHex)
     * para ofrecer una respuesta autocontenida sin necesidad de llamadas
     * adicionales.
     *
     * @param variantAttrib entidad a convertir
     * @return DTO con los datos del atributo asignado a la variante
     */
    public static ProductVariantAttribResponseDTO toResponseDTO(ProductVariantAttrib variantAttrib) {
        return ProductVariantAttribResponseDTO.builder()
                .id(variantAttrib.getId())
                .variantId(variantAttrib.getVariant().getId())
                .attribId(variantAttrib.getAttribValue().getAttrib().getId())
                .attribName(variantAttrib.getAttribValue().getAttrib().getName())
                .attribValueId(variantAttrib.getAttribValue().getId())
                .value(variantAttrib.getAttribValue().getValue())
                .label(variantAttrib.getAttribValue().getLabel())
                .colorHex(variantAttrib.getAttribValue().getColorHex())
                .build();
    }

    /**
     * Convierte una lista de entidades ProductVariantAttrib a una lista de DTOs de
     * respuesta.
     *
     * @param variantAttribs lista de entidades a convertir
     * @return lista de DTOs con los atributos asignados a la variante
     */
    public static List<ProductVariantAttribResponseDTO> toResponseDTOList(List<ProductVariantAttrib> variantAttribs) {
        if (variantAttribs == null)
            return new ArrayList<>();
        List<ProductVariantAttribResponseDTO> dtoList = new ArrayList<>();
        for (ProductVariantAttrib variantAttrib : variantAttribs) {
            dtoList.add(toResponseDTO(variantAttrib));
        }
        return dtoList;
    }
}
