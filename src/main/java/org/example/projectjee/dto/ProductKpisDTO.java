package org.example.projectjee.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductKpisDTO {
    private TopProduitDTO bestRated;
    private TopProduitDTO mostSold;              // proxy = nombreReviews
    private List<RatingBucketDTO> distribution;  // 5..1
    private long totalProduits;
}
