package es.marcha.backend.model.ecommerce.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import es.marcha.backend.model.ecommerce.Category;
import es.marcha.backend.model.ecommerce.Inventory;
import es.marcha.backend.model.ecommerce.Movement;
import es.marcha.backend.model.ecommerce.Subcategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "products", indexes = {
        // Filtro base: siempre presente en búsquedas
        @Index(name = "idx_product_active_deleted", columnList = "is_active, is_deleted"),
        // Búsqueda de texto
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_slug", columnList = "slug"),
        // Filtro de precio
        @Index(name = "idx_product_price", columnList = "price"),
        // Filtros booleanos
        @Index(name = "idx_product_featured", columnList = "is_featured"),
        // Ordenación
        @Index(name = "idx_product_created_at", columnList = "created_at"),
        @Index(name = "idx_product_sold_count", columnList = "sold_count")
})
public class Product {
    // Attribs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Version
    @Column(name = "version", nullable = false)
    private long version;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "sku")
    private String sku;
    @Column(name = "description")
    private String description;
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    @Column(name = "discount_price", nullable = false)
    private BigDecimal discountPrice;
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;
    @OneToOne(mappedBy = "product", optional = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Inventory inventory;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "product_category", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;
    @ManyToMany
    @JoinTable(name = "product_subcategory", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "subcategory_id"))
    private List<Subcategory> subcategories;
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Movement> movements;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @Column(name = "weight", nullable = false)
    private double weight;
    @Column(name = "is_digital")
    private boolean isDigital;
    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;
    @Column(name = "sold_count")
    private int soldCount;
    // Gestión de stock
    @Column(name = "stock", nullable = false)
    private int stock;
    @Builder.Default
    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 5;
    // SEO && Marketing
    @Column(name = "slug", nullable = false)
    private String slug;
    @Column(name = "meta_title", nullable = false)
    private String metaTitle;
    @Column(name = "meta_description", nullable = false)
    private String metaDescription;
    @Column(name = "views")
    private Integer views;
    @Column(name = "rating")
    private Double rating;
    @Column(name = "rating_count")
    private Double ratingCount;
    @Builder.Default
    @OneToMany(mappedBy = "product")
    @JsonManagedReference
    private List<ProductReview> reviews = new ArrayList<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "product_attrib", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "attrib_id"))
    private List<ProductAttrib> attribs;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductVariant> variants;

}
