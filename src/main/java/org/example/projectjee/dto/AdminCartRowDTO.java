package org.example.projectjee.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCartRowDTO {
    private Long id;                 // id "panier" (userId)
    private Long utilisateurId;
    private String utilisateurNom;   // ✅ NEW
    private LocalDateTime updatedAt;
    private Double total;
}