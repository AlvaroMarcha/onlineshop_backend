package es.marcha.backend.model.order;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import es.marcha.backend.model.enums.OrderStatus;
import es.marcha.backend.model.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    // Revisar en el proximom - REFACTOR
    @JsonIgnoreProperties({ "surname", "username", "email", "password", "phone", "role", "isActive",
            "isVerified", "isBanned", "isDeleted", "profileImageUrl", "lastLogin", "createdAt",
            "updatedAt", "deletedAt" })
    private User user;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(name = "total_amount")
    private double totalAmount;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "coupon_id")
    private Long couponId;
    @Column(name = "discount_amount", nullable = false)
    private double discountAmount;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "order")
    @JsonManagedReference
    @JsonIgnore
    List<Payment> payments;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItems> orderItems;
}
