package es.marcha.backend.model.ecommerce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "products")
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
    @OneToOne(mappedBy = "product", optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    private Inventory inventory;
    @ManyToMany
    @JoinTable(name = "product_subcategory", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "subcategory_id"))
    private List<Subcategory> subcategories;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @Column(name = "weight", nullable = false)
    private double weight;
    @Column(name = "digital", nullable = false)
    private boolean digital;
    @Column(name = "featured")
    private boolean featured;
    // SEO && Marketing
    @Column(name = "slug", nullable = false)
    private String slug;
    @Column(name = "meta_title", nullable = false)
    private String metaTitle;
    @Column(name = "meta_description", nullable = false)
    private String metaDescription;
    @Column(name = "views")
    private int views;
    @Column(name = "rating", nullable = false)
    private double rating;
    @Column(name = "rating_count", nullable = false)
    private int ratingCount;

}
