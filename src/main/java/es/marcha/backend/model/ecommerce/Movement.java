package es.marcha.backend.model.ecommerce;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import es.marcha.backend.model.enums.MovementType;
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
@Table(name = "movements")
public class Movement {
    // Attribs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "previous_quantity")
    private int previousQuantity;
    @Column(name = "new_quantity")
    private int newQuantity;
    @Column(name = "movement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementType movementType;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    private String notes;

}
