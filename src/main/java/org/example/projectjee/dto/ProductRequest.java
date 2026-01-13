package org.example.projectjee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String asin;
    private String title;
    private String description;
    private Double price;
    private Integer quantityAvailable;
    private Long categorieId;
    private List<String> imageUrls;
}
