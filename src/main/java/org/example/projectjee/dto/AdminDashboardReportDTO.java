package org.example.projectjee.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardReportDTO {

    // Identité
    private String adminNom;
    private String adminEmail;
    private String date; // "2026-01-03"

    // Texte
    private String resume;

    // KPIs
    private Long totalProducts;
    private Long totalCategories;
    private Double noteMoyenneGlobale;
    private String topCategorieNom;
    private Long topCategorieQuantite;

    // ➕ Ajouts demandés
    private List<TopProduitDTO> topProduits; // top 10
    private List<ProduitCategorieStatDTO> produitsParCategorie; // distribution
}
