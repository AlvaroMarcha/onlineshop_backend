package es.marcha.backend.modules.wishlist.application.mapper;

import java.util.Collections;
import java.util.List;

import es.marcha.backend.modules.wishlist.application.dto.response.WishlistItemResponseDTO;
import es.marcha.backend.modules.wishlist.application.dto.response.WishlistResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.domain.model.product.ProductImage;
import es.marcha.backend.modules.wishlist.domain.model.Wishlist;
import es.marcha.backend.modules.wishlist.domain.model.WishlistItem;

public class WishlistMapper {

    private WishlistMapper() {
    }

    /**
     * Convierte un {@link WishlistItem} en su DTO de respuesta.
     *
     * @param item ítem de wishlist a convertir
     * @return {@link WishlistItemResponseDTO} con los datos del ítem y del producto
     */
    public static WishlistItemResponseDTO toItemDTO(WishlistItem item) {
        Product product = item.getProduct();
        return WishlistItemResponseDTO.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .productPrice(product.getPrice())
                .productDiscountPrice(product.getDiscountPrice())
                .productMainImageUrl(resolveMainImageUrl(product))
                .productIsActive(product.isActive())
                .productStock(product.getStock())
                .addedAt(item.getAddedAt())
                .build();
    }

    /**
     * Convierte una {@link Wishlist} en su DTO de respuesta.
     *
     * @param wishlist entidad a convertir
     * @return {@link WishlistResponseDTO} con todos los ítems y el total
     */
    public static WishlistResponseDTO toWishlistDTO(Wishlist wishlist) {
        List<WishlistItemResponseDTO> itemDTOs = wishlist.getItems() == null
                ? Collections.emptyList()
                : wishlist.getItems().stream().map(WishlistMapper::toItemDTO).toList();

        return WishlistResponseDTO.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .items(itemDTOs)
                .totalItems(itemDTOs.size())
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .build();
    }

    /**
     * Resuelve la URL de la imagen principal del producto.
     * Si no hay imagen marcada como principal, devuelve la primera disponible.
     * Devuelve {@code null} si no hay imágenes.
     */
    private static String resolveMainImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::isMain)
                .findFirst()
                .or(() -> product.getImages().stream().findFirst())
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}
