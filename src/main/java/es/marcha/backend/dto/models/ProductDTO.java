package es.marcha.backend.dto.models;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String urlImg;
    private Integer stock;
    private BigDecimal price;
    private Boolean visible;
    private String category;
    private String subcategory;
    private List<String> images;
    private String details;
    private String specifications;


}
