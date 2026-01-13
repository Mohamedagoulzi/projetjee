package org.example.projectjee.controllers;

import org.example.projectjee.dto.ReviewRequest;
import org.example.projectjee.model.Product;
import org.example.projectjee.model.Rating;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.ProduitRepository;
import org.example.projectjee.repository.RatingRepository;
import org.example.projectjee.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(origins = "http://localhost:3000")
public class RatingController {

    private final ProduitRepository productRepository;
    private final RatingRepository ratingRepository;
    private final UtilisateurRepository utilisateurRepository;

    public RatingController(ProduitRepository productRepository,
                           RatingRepository ratingRepository,
                           UtilisateurRepository utilisateurRepository) {
        this.productRepository = productRepository;
        this.ratingRepository = ratingRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    /**
     * Récupère l'ID de l'utilisateur actuellement connecté
     */
    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // Get all reviews for a product
    @GetMapping("/{productId}/reviews")
    public List<Rating> getProductReviews(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        return ratingRepository.findByProduit(product);
    }

    // Add new review
    @PostMapping("/{productId}/reviews")
    public Rating addReview(@PathVariable Long productId, @RequestBody ReviewRequest reviewRequest) {
        // Validate input
        if (reviewRequest.getStars() == null || reviewRequest.getStars() < 1 || reviewRequest.getStars() > 5) {
            throw new RuntimeException("Les étoiles doivent être entre 1 et 5");
        }
        if (reviewRequest.getComment() == null || reviewRequest.getComment().trim().isEmpty()) {
            throw new RuntimeException("Le commentaire ne peut pas être vide");
        }
        if (reviewRequest.getComment().length() > 500) {
            throw new RuntimeException("Le commentaire ne peut pas dépasser 500 caractères");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        Utilisateur user = getCurrentUser();

        // Create rating from request
        Rating review = Rating.builder()
                .produit(product)
                .utilisateur(user)
                .stars(reviewRequest.getStars())
                .comment(reviewRequest.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return ratingRepository.save(review);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId) {

        Rating review = ratingRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));

        // Vérifier que l'utilisateur est bien le propriétaire du commentaire
        if (!review.getUtilisateur().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }

        ratingRepository.delete(review);
    }
}
