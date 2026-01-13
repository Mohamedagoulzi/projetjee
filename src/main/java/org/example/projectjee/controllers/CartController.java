package org.example.projectjee.controllers;

import org.example.projectjee.model.CartItem;
import org.example.projectjee.model.Product;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.CartItemRepository;
import org.example.projectjee.repository.ProduitRepository;
import org.example.projectjee.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/panier")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProduitRepository productRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CartController(CartItemRepository cartItemRepository,
                          ProduitRepository productRepository,
                          UtilisateurRepository utilisateurRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
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

    // 1. AJOUTER produit au panier
    @PostMapping("/add/{productId}")
    public CartItem addToCart(@PathVariable Long productId,
                              @RequestParam(defaultValue = "1") int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        Utilisateur user = getCurrentUser();
        Long userId = user.getId();

        // Chercher si produit existe déjà
        List<CartItem> itemsUser = cartItemRepository.findByUtilisateur_Id(userId);
        CartItem item = itemsUser.stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            item = CartItem.builder()
                    .utilisateur(user)
                    .product(product)
                    .quantity(quantity)
                    .build();
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        return cartItemRepository.save(item);
    }

    // 2. LISTER panier
    @GetMapping
    public List<CartItem> getCart() {
        return cartItemRepository.findByUtilisateur_Id(getCurrentUserId());
    }

    // 3. MODIFIER quantité
    @PutMapping("/{cartItemId}")
    public CartItem updateCartItemQuantity(@PathVariable Long cartItemId,
                                           @RequestParam int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item panier introuvable"));

        // Vérifier propriétaire
        if (!item.getUtilisateur().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Accès non autorisé");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    // 4. SUPPRIMER item
    @DeleteMapping("/{cartItemId}")
    public void deleteCartItem(@PathVariable Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item panier introuvable"));

        if (!item.getUtilisateur().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Accès non autorisé");
        }

        cartItemRepository.delete(item);
    }
}
