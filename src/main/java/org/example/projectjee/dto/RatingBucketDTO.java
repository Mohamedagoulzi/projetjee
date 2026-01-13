package org.example.projectjee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RatingBucketDTO {
    private int stars;   // 1..5
    private long count;
}
