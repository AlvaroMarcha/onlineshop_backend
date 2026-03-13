package es.marcha.backend.modules.dashboard.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.domain.model.product.ProductReview;
import es.marcha.backend.modules.catalog.infrastructure.persistence.ProductRepository;
import es.marcha.backend.modules.dashboard.application.dto.response.BestRatedProductDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.MostViewedProductDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.ProductSummaryDTO;
import es.marcha.backend.modules.dashboard.application.dto.response.RecentReviewDTO;
import lombok.RequiredArgsConstructor;

/**
 * Servicio especializado para métricas y operaciones del dashboard de
 * tienda/catálogo.
 * <p>
 * Proporciona estadísticas de productos, productos destacados, valoraciones y
 * reseñas.
 * Todos los métodos están cacheados y son de solo lectura.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreDashboardService {

    private final ProductRepository productRepository;

    /**
     * Obtiene el resumen de productos del catálogo.
     * <p>
     * Calcula: total de productos, activos, inactivos y sin stock.
     * </p>
     *
     * @return ProductSummaryDTO con los contadores de productos
     */
    @Cacheable(value = "productSummary")
    public ProductSummaryDTO getProductSummary() {
        List<Product> allProducts = productRepository.findAll();

        long totalProducts = allProducts.size();

        long activeProducts = allProducts.stream()
                .filter(product -> product.isActive() && !product.isDeleted())
                .count();

        long inactiveProducts = allProducts.stream()
                .filter(product -> !product.isActive() && !product.isDeleted())
                .count();

        long outOfStockProducts = allProducts.stream()
                .filter(product -> !product.isDeleted() && product.getStock() == 0)
                .count();

        return ProductSummaryDTO.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .inactiveProducts(inactiveProducts)
                .outOfStockProducts(outOfStockProducts)
                .build();
    }

    /**
     * Obtiene los productos más visitados del catálogo.
     * <p>
     * Retorna productos ordenados por número de vistas en orden descendente.
     * Solo incluye productos activos y no eliminados.
     * </p>
     *
     * @param limit Número máximo de productos a retornar
     * @return Lista de MostViewedProductDTO ordenada por vistas (descendente)
     */
    @Cacheable(value = "mostViewedProducts")
    public List<MostViewedProductDTO> getMostViewedProducts(int limit) {
        return productRepository.findAll().stream()
                .filter(product -> product.isActive() && !product.isDeleted())
                .filter(product -> product.getViews() != null && product.getViews() > 0)
                .sorted(Comparator.comparing(Product::getViews, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(product -> {
                    String imageUrl = null;
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        imageUrl = product.getImages().get(0).getUrl();
                    }

                    return MostViewedProductDTO.builder()
                            .productId(product.getId())
                            .name(product.getName())
                            .sku(product.getSku())
                            .price(product.getPrice())
                            .views(product.getViews() != null ? product.getViews() : 0)
                            .imageUrl(imageUrl)
                            .stock(product.getStock())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los productos mejor valorados del catálogo.
     * <p>
     * Retorna productos con el rating promedio más alto, calculado a partir de sus
     * reviews.
     * Solo incluye productos activos, no eliminados y con al menos 1 review.
     * </p>
     *
     * @param limit Número máximo de productos a retornar
     * @return Lista de BestRatedProductDTO ordenada por rating (descendente)
     */
    @Cacheable(value = "bestRatedProducts")
    public List<BestRatedProductDTO> getBestRatedProducts(int limit) {
        return productRepository.findAll().stream()
                .filter(product -> product.isActive() && !product.isDeleted())
                .filter(product -> product.getReviews() != null && !product.getReviews().isEmpty())
                .filter(product -> product.getReviews().stream().anyMatch(r -> r.isActive() && !r.isDeleted()))
                .map(product -> {
                    // Calcular rating promedio de reviews activas
                    List<ProductReview> activeReviews = product.getReviews().stream()
                            .filter(review -> review.isActive() && !review.isDeleted())
                            .collect(Collectors.toList());

                    double averageRating = activeReviews.stream()
                            .mapToInt(ProductReview::getRating)
                            .average()
                            .orElse(0.0);

                    String imageUrl = null;
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        imageUrl = product.getImages().get(0).getUrl();
                    }

                    return BestRatedProductDTO.builder()
                            .productId(product.getId())
                            .name(product.getName())
                            .sku(product.getSku())
                            .price(product.getPrice())
                            .averageRating(averageRating)
                            .reviewCount(activeReviews.size())
                            .imageUrl(imageUrl)
                            .stock(product.getStock())
                            .build();
                })
                .sorted(Comparator.comparing(BestRatedProductDTO::getAverageRating, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las reseñas más recientes del catálogo.
     * <p>
     * Retorna las últimas reseñas creadas, ordenadas por fecha de creación (más
     * recientes primero).
     * Solo incluye reviews activas de productos activos.
     * </p>
     *
     * @param limit Número máximo de reseñas a retornar
     * @return Lista de RecentReviewDTO ordenada por fecha (descendente)
     */
    @Cacheable(value = "recentReviews")
    public List<RecentReviewDTO> getRecentReviews(int limit) {
        return productRepository.findAll().stream()
                .filter(product -> product.isActive() && !product.isDeleted())
                .filter(product -> product.getReviews() != null && !product.getReviews().isEmpty())
                .flatMap(product -> product.getReviews().stream()
                        .filter(review -> review.isActive() && !review.isDeleted())
                        .map(review -> {
                            String productImageUrl = null;
                            if (product.getImages() != null && !product.getImages().isEmpty()) {
                                productImageUrl = product.getImages().get(0).getUrl();
                            }

                            String userName = "Usuario";
                            if (review.getUser() != null) {
                                userName = review.getUser().getName() + " " + review.getUser().getSurname();
                            }

                            return RecentReviewDTO.builder()
                                    .reviewId(review.getId())
                                    .productId(product.getId())
                                    .productName(product.getName())
                                    .rating(review.getRating())
                                    .comment(review.getComment())
                                    .createdAt(review.getCreatedAt())
                                    .userName(userName)
                                    .productImageUrl(productImageUrl)
                                    .build();
                        }))
                .sorted(Comparator.comparing(RecentReviewDTO::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
