package es.marcha.backend.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.marcha.backend.model.ecommerce.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "order_items")
public class OrderItems {
    // Attribs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "sku")
    private String sku;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "discount_price")
    private BigDecimal discountPrice;
    @Column(name = "stock")
    private int quantity;
    @Column(name = "weight")
    private double weight;
    @Column(name = "is_digital")
    private boolean isDigital;
    @Column(name = "is_featured")
    private boolean isFeatured;
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "is_deleted")
    private boolean isDeleted;
    @Column(name = "views")
    private Integer views;
    @Column(name = "sold_count")
    private int soldCount;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
