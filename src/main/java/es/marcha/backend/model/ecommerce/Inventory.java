package es.marcha.backend.model.ecommerce;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "inventory")
public class Inventory {
    // Attribs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Version
    @Column(name = "version")
    private int version;
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonManagedReference
    private Product product;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "reserved_quantity")
    private int reservedQuantity;
    @Column(name = "min_stock", nullable = false)
    private int minStock;
    @Column(name = "max_stock")
    private int maxStock;
    @Column(name = "incoming_stock")
    private int incomingStock;
    @Column(name = "damaged_stock")
    private int damagedStock;
    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
