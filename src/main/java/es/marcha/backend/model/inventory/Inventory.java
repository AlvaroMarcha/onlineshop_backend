package es.marcha.backend.model.inventory;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType typeMovement;

    @Column(length = 255)
    private String reason;

    private Integer stockBefore;
    private Integer stockAfter;

    @Column(nullable = false)
    private LocalDateTime dateMovement = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProducto() { return producto; }
    public void setProducto(Product producto) { this.producto = producto; }
    public MovementType getTypeMovement() { return typeMovement; }
    public void setTypeMovement(MovementType typeMovement) { this.typeMovement = typeMovement; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getStockBefore() { return stockBefore; }
    public void setStockBefore(Integer stockBefore) { this.stockBefore = stockBefore; }
    public Integer getStockAfter() { return stockAfter; }
    public void setStockAfter(Integer stockAfter) { this.stockAfter = stockAfter; }
    public LocalDateTime getDateMovement() { return dateMovement; }
    public void setDateMovement(LocalDateTime dateMovement) { this.dateMovement = dateMovement; }


}
