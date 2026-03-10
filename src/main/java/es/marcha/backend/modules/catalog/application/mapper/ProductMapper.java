package es.marcha.backend.modules.catalog.application.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import es.marcha.backend.modules.catalog.application.dto.request.ProductRequestDTO;
import es.marcha.backend.modules.catalog.application.dto.response.product.ProductImageResponseDTO;
import es.marcha.backend.modules.catalog.application.dto.response.product.ProductResponseDTO;
import es.marcha.backend.modules.catalog.domain.model.Category;
import es.marcha.backend.modules.catalog.domain.model.Subcategory;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.domain.model.product.ProductImage;

public class ProductMapper {

        public static ProductResponseDTO toProductDTO(Product product) {
                return toProductDTO(product, null);
        }

        /**
         * Convierte un producto a DTO enriqueciendo el campo {@code isInWishlist}.
         * Usar este método cuando se dispone de contexto de usuario autenticado.
         *
         * @param product      entidad a convertir
         * @param isInWishlist {@code true} si el producto está en la wishlist del
         *                     usuario,
         *                     {@code false} si no, o {@code null} si no hay usuario
         *                     autenticado
         * @return DTO con el campo {@code isInWishlist} establecido
         */
        public static ProductResponseDTO toProductDTO(Product product, Boolean isInWishlist) {
                return ProductResponseDTO.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .sku(product.getSku())
                                .categories(deriveCategoriesFromSubcategories(product))
                                .description(product.getDescription())
                                .reviews(product.getReviews() != null
                                                ? product.getReviews().stream()
                                                                .map(ProductReviewMapper::toProductReviewDTO).toList()
                                                : Collections.emptyList())
                                .price(product.getPrice())
                                .discountPrice(product.getDiscountPrice())
                                .taxRate(product.getTaxRate())
                                .weight(product.getWeight())
                                .isDigital(product.isDigital())
                                .isFeatured(product.isFeatured())
                                .slug(product.getSlug())
                                .metaTitle(product.getMetaTitle())
                                .metaDescription(product.getMetaDescription())
                                .rating(product.getRating())
                                .ratingCount(product.getRatingCount())
                                .soldCount(product.getSoldCount())
                                .stock(product.getStock())
                                .lowStockThreshold(product.getLowStockThreshold())
                                .isActive(product.isActive())
                                .attribs(product.getAttribs() != null
                                                ? ProductAttribMapper.toResponseDTOList(product.getAttribs())
                                                : Collections.emptyList())
                                .variants(Collections.emptyList())
                                .mainImageUrl(resolveMainImageUrl(product))
                                .isInWishlist(isInWishlist)
                                .build();
        }

        /**
         * Convierte un producto a DTO incluyendo la lista completa de variantes.
         * Usar exclusivamente en el endpoint de detalle de producto.
         *
         * @param product entidad a convertir
         * @return DTO con todos los campos incluidas las variantes
         */
        public static ProductResponseDTO toProductDetailDTO(Product product) {
                return toProductDetailDTO(product, null);
        }

        /**
         * Convierte un producto a DTO de detalle enriqueciendo el campo
         * {@code isInWishlist}.
         *
         * @param product      entidad a convertir
         * @param isInWishlist {@code true} si el producto está en la wishlist del
         *                     usuario,
         *                     {@code false} si no, o {@code null} si no hay usuario
         *                     autenticado
         * @return DTO con todos los campos incluidas las variantes e
         *         {@code isInWishlist}
         */
        public static ProductResponseDTO toProductDetailDTO(Product product, Boolean isInWishlist) {
                return ProductResponseDTO.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .sku(product.getSku())
                                .categories(deriveCategoriesFromSubcategories(product))
                                .description(product.getDescription())
                                .reviews(product.getReviews() != null
                                                ? product.getReviews().stream()
                                                                .map(ProductReviewMapper::toProductReviewDTO).toList()
                                                : Collections.emptyList())
                                .price(product.getPrice())
                                .discountPrice(product.getDiscountPrice())
                                .taxRate(product.getTaxRate())
                                .weight(product.getWeight())
                                .isDigital(product.isDigital())
                                .isFeatured(product.isFeatured())
                                .slug(product.getSlug())
                                .metaTitle(product.getMetaTitle())
                                .metaDescription(product.getMetaDescription())
                                .rating(product.getRating())
                                .ratingCount(product.getRatingCount())
                                .soldCount(product.getSoldCount())
                                .stock(product.getStock())
                                .lowStockThreshold(product.getLowStockThreshold())
                                .isActive(product.isActive())
                                .attribs(product.getAttribs() != null
                                                ? ProductAttribMapper.toResponseDTOList(product.getAttribs())
                                                : Collections.emptyList())
                                .variants(product.getVariants() != null
                                                ? ProductVariantMapper.toResponseDTOList(product.getVariants())
                                                : Collections.emptyList())
                                .mainImageUrl(resolveMainImageUrl(product))
                                .images(product.getImages() != null
                                                ? product.getImages().stream()
                                                                .map(ProductMapper::toProductImageDTO)
                                                                .toList()
                                                : Collections.emptyList())
                                .isInWishlist(isInWishlist)
                                .build();
        }

        /** Convierte una {@link ProductImage} en su DTO de respuesta. */
        public static ProductImageResponseDTO toProductImageDTO(ProductImage image) {
                return ProductImageResponseDTO.builder()
                                .id(image.getId())
                                .url(image.getUrl())
                                .altText(image.getAltText())
                                .sortOrder(image.getSortOrder())
                                .isMain(image.isMain())
                                .uploadedAt(image.getUploadedAt())
                                .build();
        }

        /**
         * Resuelve la URL de la imagen principal ({@code isMain = true}) del producto.
         * Si no hay ninguna marcada como principal, devuelve la de menor
         * {@code sortOrder}.
         * Devuelve {@code null} si el producto no tiene imágenes.
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

        /**
         * Obtiene las categorías de un producto de forma inteligente.
         * <p>
         * Implementa Opción B (híbrida):
         * - Si tiene subcategorías: deriva categorías desde subcategorías (Opción 1)
         * - Si NO tiene subcategorías: usa categorías directas del producto
         * </p>
         *
         * @param product el producto del cual obtener las categorías
         * @return lista de categorías (derivadas o directas),
         *         o lista vacía si el producto no tiene ni subcategorías ni categorías
         */
        @SuppressWarnings("deprecation") // Permitido usar getCategories() como fallback cuando no hay subcategorías
        private static List<Category> deriveCategoriesFromSubcategories(Product product) {
                // Caso 1: Si tiene subcategorías → Derivar desde subcategorías
                if (product.getSubcategories() != null && !product.getSubcategories().isEmpty()) {
                        return product.getSubcategories().stream()
                                        .map(Subcategory::getCategory)
                                        .distinct()
                                        .collect(Collectors.toList());
                }

                // Caso 2: Si NO tiene subcategorías → Usar categorías directas
                if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                        return product.getCategories();
                }

                // Caso 3: No tiene ni subcategorías ni categorías
                return Collections.emptyList();
        }

        @SuppressWarnings("deprecation") // Permitido en mapper para construir producto desde DTO
        public static Product toProductByRequestProduct(ProductRequestDTO productDTO, List<Subcategory> subcategories,
                        List<Category> categories) {
                // El SKU se genera automáticamente en ProductService — no se lee del DTO
                return Product.builder()
                                .name(productDTO.getName())
                                .description(productDTO.getDescription())
                                .price(productDTO.getPrice())
                                .discountPrice(productDTO.getDiscountPrice())
                                .taxRate(productDTO.getTaxRate())
                                .weight(productDTO.getWeight())
                                .createdBy(productDTO.getCreatedBy())
                                .isDigital(productDTO.isDigital())
                                .isFeatured(productDTO.isFeatured())
                                .subcategories(subcategories)
                                .categories(categories)
                                .stock(productDTO.getStock())
                                .build();
        }
}
