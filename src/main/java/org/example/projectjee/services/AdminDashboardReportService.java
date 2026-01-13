package org.example.projectjee.services;

import lombok.RequiredArgsConstructor;
import org.example.projectjee.dto.AdminDashboardReportDTO;
import org.example.projectjee.dto.ProduitCategorieStatDTO;
import org.example.projectjee.dto.TopCategorieDTO;
import org.example.projectjee.dto.TopProduitDTO;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.CategorieRepository;
import org.example.projectjee.repository.ProduitRepository;
import org.example.projectjee.repository.UtilisateurRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardReportService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final VenteService venteService;

    public AdminDashboardReportDTO buildReport(Authentication auth) {

        // 1) admin depuis token
        String email = auth.getName();
        Utilisateur admin = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        // 2) KPIs
        Long totalProducts = produitRepository.count();
        Long totalCategories = categorieRepository.count();
        Double note = produitRepository.findNoteMoyenneGlobale();
        if (note == null) note = 0.0;

        TopCategorieDTO topCat = venteService.getTopCategorie();
        String topNom = (topCat != null && topCat.getCategorieNom() != null) ? topCat.getCategorieNom() : "-";
        Long topQte = (topCat != null) ? topCat.getTotalQte() : 0L;

        // 3) Distribution catégories
        List<ProduitCategorieStatDTO> prodParCat = produitRepository.countProduitsParCategorie();

        // 4) Top 10 produits (tu peux changer le tri si tu veux: ratingCount desc etc.)
        List<TopProduitDTO> topProduits =
                produitRepository.findTop10DTO(PageRequest.of(0, 10));

        // 👉 si tu n’as pas findTop10DTO, je te donne une alternative juste après

        String resume = "Ce rapport synthétise l’état actuel de la plateforme (catalogue produits, "
                + "structure des catégories, et indicateurs de satisfaction). "
                + "Il constitue un instantané de suivi pour l’administration.";

        return AdminDashboardReportDTO.builder()
                .adminNom(admin.getNom())
                .adminEmail(admin.getEmail())
                .date(LocalDate.now().toString())
                .resume(resume)
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .noteMoyenneGlobale(note)
                .topCategorieNom(topNom)
                .topCategorieQuantite(topQte)
                .produitsParCategorie(prodParCat)
                .topProduits(topProduits)
                .build();
    }
}
