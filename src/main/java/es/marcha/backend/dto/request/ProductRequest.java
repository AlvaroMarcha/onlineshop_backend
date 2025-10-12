package es.marcha.backend.dto.request;

import java.math.BigDecimal;
import java.util.List;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    private String name;
    private String description;
    private String urlImg;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private Long subcategoryId;
    private List<Long> values;
    private List<String> images;
    private String details;
    private String specifications;
}
