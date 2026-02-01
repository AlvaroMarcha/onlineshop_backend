package es.marcha.backend.model.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import es.marcha.backend.model.enums.OrderStatus;
import es.marcha.backend.model.user.User;
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
@Setter
@Getter
@Builder
@Entity
@Table(name = "orders")
public class Order {
    // Attribs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    // Convertir lo que se devuelve en un DTO especifico mas adelante (Esto es para salir del paso)
    @JsonIgnoreProperties({"surname", "username", "email", "password", "phone", "role", "isActive",
            "isVerified", "isBanned", "isDeleted", "profileImageUrl", "lastLogin", "createdAt",
            "updatedAt", "deletedAt"})
    private User user;
    // 1. CREATED 2. PAID 3. PROCESSING 4. SHIPPED 5. DELIVERED 6. CANCELLED 7. RETURNED
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(name = "total_amount")
    private double totalAmount;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;

}
