package org.example.projectjee.controllers;

import org.example.projectjee.model.*;
import org.example.projectjee.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProduitRepository productRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityManager entityManager;

    public OrderController(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          CartItemRepository cartItemRepository,
                          ProduitRepository productRepository,
                          UtilisateurRepository utilisateurRepository,
                          EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.entityManager = entityManager;
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

    /**
     * Créer une commande depuis le panier (Processus de paiement)
     * POST /api/ventes
     */
    @Transactional
    @PostMapping("/ventes")
    public ResponseEntity<?> createOrder() {
        // Récupérer l'utilisateur connecté
        Utilisateur user = getCurrentUser();
        Long userId = user.getId();

        // Récupérer les items du panier
        List<CartItem> cartItems = cartItemRepository.findByUtilisateur_Id(userId);

        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Le panier est vide"));
        }

        // VALIDATION DU STOCK: Vérifier la disponibilité pour tous les produits AVANT de créer la commande
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable: " + cartItem.getProduct().getId()));
            
            // Vérifier que le stock est suffisant
            if (product.getQuantityAvailable() == null || product.getQuantityAvailable() < cartItem.getQuantity()) {
                String errorMsg = product.getQuantityAvailable() == null || product.getQuantityAvailable() == 0 
                    ? "Produit en rupture de stock: " + product.getTitle()
                    : "Stock insuffisant pour " + product.getTitle() + ". Disponible: " + product.getQuantityAvailable() + ", Demandé: " + cartItem.getQuantity();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(errorMsg));
            }
        }

        // Calculer le montant total
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // Créer la commande
        Order order = Order.builder()
                .utilisateur(user)
                .createdAt(LocalDateTime.now())
                .totalAmount(totalAmount)
                .build();

        order = orderRepository.save(order);

        // Créer les lignes de commande ET décrémenter le stock
        for (CartItem cartItem : cartItems) {
            // Recharger le produit pour obtenir la version la plus récente (éviter les conditions de concurrence)
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));
            
            // Double vérification du stock (protection contre les conditions de concurrence)
            if (product.getQuantityAvailable() == null || product.getQuantityAvailable() < cartItem.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour " + product.getTitle());
            }
            
            // Décrémenter le stock
            product.setQuantityAvailable(product.getQuantityAvailable() - cartItem.getQuantity());
            productRepository.save(product);
            
            // Créer la ligne de commande
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Vider le panier
        cartItemRepository.deleteAll(cartItems);

        // Flush pour s'assurer que tout est persisté
        entityManager.flush();
        entityManager.clear();

        // Recharger la commande avec les items
        order = orderRepository.findById(order.getId())
                .orElse(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Créer une réponse d'erreur structurée
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 400);
        error.put("error", "Bad Request");
        error.put("message", message);
        return error;
    }

    /**
     * Récupérer les commandes de l'utilisateur
     * GET /api/commandes
     */
    @GetMapping("/commandes")
    public List<Order> getUserOrders() {
        return orderRepository.findByUtilisateurId(getCurrentUserId());
    }

    /**
     * Récupérer une commande spécifique
     * GET /api/commandes/{id}
     */
    @GetMapping("/commandes/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        // Vérifier que c'est bien la commande de l'utilisateur connecté
        if (!order.getUtilisateur().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Accès non autorisé");
        }

        return ResponseEntity.ok(order);
    }
}
