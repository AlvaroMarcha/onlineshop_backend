package es.marcha.backend.model.ecommerce.product;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference("product-images")
    private Product product;
    @Column(name = "url", nullable = false)
    private String url;
    @Column(name = "filename", nullable = false)
    private String filename;
    @Column(name = "alt_text")
    private String altText;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(name = "is_main", nullable = false)
    private boolean isMain;
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
