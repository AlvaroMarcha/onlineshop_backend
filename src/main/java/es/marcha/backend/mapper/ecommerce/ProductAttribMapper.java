package es.marcha.backend.mapper.ecommerce;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.marcha.backend.dto.request.ecommerce.product.ProductAttribRequestDTO;
import es.marcha.backend.dto.response.ecommerce.product.attrib.ProductAttribResponseDTO;
import es.marcha.backend.model.ecommerce.product.ProductAttrib;

public class ProductAttribMapper {

    /**
     * Convierte una entidad ProductAttrib a su DTO de respuesta.
     *
     * @param productAttrib entidad a convertir
     * @return DTO con los datos del atributo y sus valores
     */
    public static ProductAttribResponseDTO toResponseDTO(ProductAttrib productAttrib) {
        return ProductAttribResponseDTO.builder()
                .id(productAttrib.getId())
                .name(productAttrib.getName())
                .description(productAttrib.getDescription())
                .slug(productAttrib.getSlug())
                .type(productAttrib.getType())
                .isRequired(productAttrib.isRequired())
                .sortOrder(productAttrib.getSortOrder())
                .values(ProductAttribValueMapper.toResponseDTOList(productAttrib.getValues()))
                .createdAt(productAttrib.getCreatedAt())
                .build();
    }

    /**
     * Convierte una lista de entidades ProductAttrib a una lista de DTOs de
     * respuesta.
     *
     * @param attribs lista de entidades a convertir
     * @return lista de DTOs con los datos de cada atributo y sus valores
     */
    public static List<ProductAttribResponseDTO> toResponseDTOList(List<ProductAttrib> attribs) {
        if (attribs == null) return new ArrayList<>();
        List<ProductAttribResponseDTO> dtoList = new ArrayList<>();
        for (ProductAttrib attrib : attribs) {
            dtoList.add(toResponseDTO(attrib));
        }
        return dtoList;
    }

    /**
     * Convierte un DTO de solicitud en una nueva entidad ProductAttrib.
     * Los campos gestionados por el sistema (id, createdAt, updatedAt) no se
     * asignan aquí.
     *
     * @param dto DTO con los datos del atributo a crear
     * @return nueva entidad ProductAttrib sin persistir
     */
    public static ProductAttrib fromRequestDTO(ProductAttribRequestDTO dto) {
        return ProductAttrib.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .type(dto.getType())
                .isRequired(dto.isRequired())
                .sortOrder(dto.getSortOrder())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
