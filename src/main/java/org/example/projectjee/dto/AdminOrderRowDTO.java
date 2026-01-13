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
public class AdminOrderRowDTO {
    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;     // ✅ NEW
    private LocalDateTime dateCreation;
    private Double montantTotal;
}