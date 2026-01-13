package org.example.projectjee.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.example.projectjee.dto.AdminDashboardReportDTO;
import org.example.projectjee.dto.CategorieDTO;
import org.example.projectjee.dto.ProduitCategorieStatDTO;
import org.example.projectjee.dto.TopCategorieDTO;
import org.example.projectjee.dto.TopProduitDTO;
import org.example.projectjee.dto.UserChecklistDTO;
import org.example.projectjee.dto.UserResponse;
import org.example.projectjee.model.Product;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.CategorieRepository;
import org.example.projectjee.repository.ProduitRepository;
import org.example.projectjee.repository.UtilisateurRepository;
import org.example.projectjee.services.VenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/utilisateur")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final UtilisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;   // ⬅️ AJOUT
    private final CategorieRepository categorieRepository;   // ⬅️ AJOUT
    private final VenteService venteService;


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        // 0️⃣ Sécurité : si pas d'utilisateur authentifié
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Utilisateur non authentifié");
        }

        // 1️⃣ Récupérer l'email depuis le token (username)
        String email = authentication.getName();

        // 2️⃣ Chercher l'utilisateur en base
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 3️⃣ Construire la réponse
        UserResponse dto = new UserResponse(
                user.getNom(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(dto);
    }


        // 1️⃣ KPI : Total des Produits
    @GetMapping("/total-produits")
    public ResponseEntity<Long> getTotalProduits() {
        long total = produitRepository.count();   // compte toutes les lignes de la table PRODUIT
        System.out.println("TOTAL PRODUITS = " + total);
        return ResponseEntity.ok(total);
    }

    // 2️⃣ KPI : Total des Catégories
    @GetMapping("/total-categories")
    public ResponseEntity<Long> getTotalCategories() {
        long total = categorieRepository.count();   // compte toutes les lignes dans la table categorie
        System.out.println("TOTAL CATEGORIES = " + total);
        return ResponseEntity.ok(total);
    }
    // 3️⃣ KPI : Note moyenne globale
    @GetMapping("/note-moyenne-globale")
    public ResponseEntity<Double> getNoteMoyenneGlobale() {
        Double note = produitRepository.findNoteMoyenneGlobale();
        if (note == null) {
            note = 0.0;
        }
        System.out.println("NOTE MOYENNE GLOBALE = " + note);
        return ResponseEntity.ok(note);
    }
    @GetMapping("/categories")
    public ResponseEntity<List<CategorieDTO>> getAllCategories() {
        List<CategorieDTO> dtos = categorieRepository.findAll().stream()
            .map(c -> new CategorieDTO(
                    c.getId(),
                    c.getNom()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    


    @GetMapping("/topCategorie")
    public TopCategorieDTO topCategorie() {
        return venteService.getTopCategorie();
    }



    @GetMapping("/top-produits")
    public ResponseEntity<List<TopProduitDTO>> getTopProduits(
            @RequestParam(name = "categorieId", required = false) Long categorieId) {

        List<Product> produits;

        if (categorieId == null) {
            // Top 10 global
            produits = produitRepository.findTop10ByOrderByPriceDesc();
        } else {
            // Top 10 pour une catégorie donnée
            produits = produitRepository.findTop10ByCategorie_IdOrderByPriceDesc(categorieId);
        }

        List<TopProduitDTO> dtos = produits.stream()
                .map(p -> new TopProduitDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getCategorie() != null ? p.getCategorie().getNom() : null,
                        p.getPrice() != null ? BigDecimal.valueOf(p.getPrice()) : null,
                        p.getRating() != null ? BigDecimal.valueOf(p.getRating()) : null,
                        p.getRatingCount() != null ? p.getRatingCount().intValue() : null
                ))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/produits-par-categorie")
    public ResponseEntity<List<ProduitCategorieStatDTO>> getProduitsParCategorie() {
        List<ProduitCategorieStatDTO> stats = produitRepository.countProduitsParCategorie();
        return ResponseEntity.ok(stats);
    }


        // 🔹 Liste simplifiée des utilisateurs (pour la checklist)
    @GetMapping("/all")
    public ResponseEntity<List<UserChecklistDTO>> getAllUsers() {

        List<UserChecklistDTO> dtos = utilisateurRepository.findAll()
                .stream()
                .map(u -> UserChecklistDTO.builder()
                        .id(u.getId())
                        .nom(u.getNom())
                        .email(u.getEmail())
                        .dateCreation(u.getDateCreation()) // peut être null si ancien user
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .build()
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }




    @GetMapping("/dashboard-report")
    public ResponseEntity<AdminDashboardReportDTO> dashboardReport(Authentication authentication) {

        String email = authentication.getName();
        Utilisateur admin = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Long totalProducts = produitRepository.count();
        Long totalCategories = categorieRepository.count();
        Double note = produitRepository.findNoteMoyenneGlobale();
        if (note == null) note = 0.0;

        TopCategorieDTO topCat = venteService.getTopCategorie(); // {categorieNom, totalQte}

        String topNom = (topCat != null && topCat.getCategorieNom() != null)
                ? topCat.getCategorieNom()
                : "-";

        long topQte = (topCat != null)
                ? topCat.getTotalQte()
                : 0L;


        String resume = "Ce rapport synthétise l’état actuel de la plateforme : catalogue produits, structure des catégories, "
            + "et indicateurs de satisfaction (notes/avis). Il sert d’instantané de suivi pour l’administration.";

        AdminDashboardReportDTO dto = AdminDashboardReportDTO.builder()
            .adminNom(admin.getNom())
            .adminEmail(admin.getEmail())
            .totalProducts(totalProducts)
            .totalCategories(totalCategories)
            .noteMoyenneGlobale(note)
            .topCategorieNom(topNom)
            .topCategorieQuantite(topQte)
            .resume(resume)
            .build();

        return ResponseEntity.ok(dto);
    }




}
