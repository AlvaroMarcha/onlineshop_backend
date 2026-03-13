package es.marcha.backend.modules.catalog.application.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.marcha.backend.modules.catalog.application.dto.request.product.ProductVariantRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.response.product.variant.ProductVariantResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.domain.model.product.ProductVariant;

public class ProductVariantMapper {

    /**
     * Convierte una entidad ProductVariant a su DTO de respuesta.
     * Los atributos asignados a la variante se mapean a través de
     * ProductVariantAttribMapper.
     *
     * @param variant entidad a convertir
     * @return DTO con los datos de la variante y sus atributos
     */
    public static ProductVariantResponseDTO toResponseDTO(ProductVariant variant) {
        return ProductVariantResponseDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .sku(variant.getSku())
                .priceOverride(variant.getPriceOverride())
                .discountPriceOverride(variant.getDiscountPriceOverride())
                .stock(variant.getStock())
                .isDefault(variant.isDefault())
                .isActive(variant.isActive())
                .attribs(ProductVariantAttribMapper.toResponseDTOList(variant.getAttribs()))
                .createdAt(variant.getCreatedAt())
                .build();
    }

    /**
     * Convierte una lista de entidades ProductVariant a una lista de DTOs de
     * respuesta.
     *
     * @param variants lista de entidades a convertir
     * @return lista de DTOs con los datos de cada variante
     */
    public static List<ProductVariantResponseDTO> toResponseDTOList(List<ProductVariant> variants) {
        if (variants == null)
            return new ArrayList<>();
        List<ProductVariantResponseDTO> dtoList = new ArrayList<>();
        for (ProductVariant variant : variants) {
            dtoList.add(toResponseDTO(variant));
        }
        return dtoList;
    }

    /**
     * Convierte un DTO de solicitud en una nueva entidad ProductVariant asociada a
     * un producto.
     * Los attribValueIds del DTO se resuelven a nivel de servicio; aquí solo se
     * mapean
     * los campos escalares de la variante.
     *
     * @param dto     DTO con los datos de la variante a crear
     * @param product producto al que pertenece la variante
     * @return nueva entidad ProductVariant sin persistir
     */
    public static ProductVariant fromRequestDTO(ProductVariantRequestDTO dto, Product product) {
        return ProductVariant.builder()
                .product(product)
                .priceOverride(dto.getPriceOverride())
                .discountPriceOverride(dto.getDiscountPriceOverride())
                .stock(dto.getStock())
                .isDefault(dto.isDefault())
                .isActive(dto.isActive())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
