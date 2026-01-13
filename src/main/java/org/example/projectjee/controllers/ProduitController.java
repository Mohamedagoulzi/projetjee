package org.example.projectjee.controllers;

import org.example.projectjee.dto.ProductKpisDTO;
import org.example.projectjee.model.Categorie;
import org.example.projectjee.model.Product;
import org.example.projectjee.repository.*;
import org.example.projectjee.services.ProduitKpiService;
import org.example.projectjee.spec.ProduitSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(origins = "http://localhost:3000")
public class ProduitController {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final ProduitKpiService produitKpiService;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final RatingRepository ratingRepository;
    private final ProductImageRepository productImageRepository;

    public ProduitController(ProduitRepository produitRepository,
            CategorieRepository categorieRepository,
            ProduitKpiService produitKpiService,
            CartItemRepository cartItemRepository,
            OrderItemRepository orderItemRepository,
            RatingRepository ratingRepository,
            ProductImageRepository productImageRepository) {
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
        this.produitKpiService = produitKpiService;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.ratingRepository = ratingRepository;
        this.productImageRepository = productImageRepository;
    }

    // ✅ READ ALL (sans filtres)
    @GetMapping
    public List<Product> getAll() {
        return produitRepository.findAll();
    }

    // ✅ READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        return produitRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product p) {
        if (p.getCategorie() != null && p.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(p.getCategorie().getId())
                    .orElse(null);
            if (cat == null) {
                return ResponseEntity.badRequest().body("Categorie introuvable");
            }
            p.setCategorie(cat);
        }
        Product saved = produitRepository.save(p);
        return ResponseEntity.ok(saved);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product body) {
        return produitRepository.findById(id).map(existing -> {
            existing.setAsin(body.getAsin());
            existing.setTitle(body.getTitle());
            existing.setDescription(body.getDescription());
            existing.setPrice(body.getPrice());
            existing.setRating(body.getRating());
            existing.setRatingCount(body.getRatingCount());
            existing.setRank(body.getRank());
            existing.setImageUrl(body.getImageUrl());
            existing.setNo_sellers(body.getNo_sellers());

            if (body.getCategorie() != null && body.getCategorie().getId() != null) {
                Categorie cat = categorieRepository
                        .findById(body.getCategorie().getId())
                        .orElse(null);
                if (cat == null) {
                    return ResponseEntity.badRequest().body("Categorie introuvable");
                }
                existing.setCategorie(cat);
            }

            Product saved = produitRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ✅ DELETE - Corrigé avec suppression des dépendances
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return produitRepository.findById(id).map(product -> {
            try {
                // 1. Supprimer les images du produit
                productImageRepository.deleteByProduct(product);

                // 2. Supprimer les avis/ratings du produit
                List<org.example.projectjee.model.Rating> ratings = ratingRepository.findByProduit(product);
                ratingRepository.deleteAll(ratings);

                // 3. Supprimer les items du panier contenant ce produit
                cartItemRepository.deleteByProduct_Id(id);

                // 4. Mettre à NULL ou supprimer les OrderItems (selon votre logique métier)
                // Option A: Mettre le produit à NULL (garder l'historique des commandes)
                orderItemRepository.setProductToNullByProductId(id);

                // Option B: Supprimer les OrderItems (perd l'historique) - décommentez si
                // préféré
                // orderItemRepository.deleteByProduct_Id(id);

                // 5. Supprimer le produit
                produitRepository.delete(product);

                return ResponseEntity.ok("Produit supprimé avec succès");
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body("Erreur lors de la suppression: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // ...existing code... (KPIs et search restent inchangés)

    @GetMapping("/kpis")
    public ProductKpisDTO kpis(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) BigDecimal noteMin,
            @RequestParam(required = false) BigDecimal noteMax,
            @RequestParam(required = false) Integer reviewsMin,
            @RequestParam(required = false) Integer reviewsMax) {
        return produitKpiService.getKpis(
                q, categorieId, prixMin, prixMax, noteMin, noteMax, reviewsMin, reviewsMax);
    }

    @GetMapping("/search")
    public List<Product> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) BigDecimal noteMin,
            @RequestParam(required = false) BigDecimal noteMax,
            @RequestParam(required = false) Integer reviewsMin,
            @RequestParam(required = false) Integer reviewsMax,
            @RequestParam(required = false, defaultValue = "note_desc") String sort) {
        Specification<Product> spec = ProduitSpecifications.withFilters(
                q, categorieId, prixMin, prixMax, noteMin, noteMax, reviewsMin, reviewsMax);

        Sort s;
        switch (sort) {
            case "reviews_desc" -> s = Sort.by(Sort.Direction.DESC, "ratingCount");
            case "prix_asc" -> s = Sort.by(Sort.Direction.ASC, "price");
            case "prix_desc" -> s = Sort.by(Sort.Direction.DESC, "price");
            default -> s = Sort.by(Sort.Direction.DESC, "rating");
        }
        return produitRepository.findAll(spec, s);
    }
}