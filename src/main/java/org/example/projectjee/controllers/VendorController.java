package org.example.projectjee.controllers;

import org.example.projectjee.dto.ProductRequest;
import org.example.projectjee.dto.ProductStatsResponse;
import org.example.projectjee.model.*;
import org.example.projectjee.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendeur")
@CrossOrigin(origins = "http://localhost:3000")
public class VendorController {

    private final ProduitRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategorieRepository categorieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntityManager entityManager;

    public VendorController(ProduitRepository productRepository,
                           ProductImageRepository productImageRepository,
                           CategorieRepository categorieRepository,
                           UtilisateurRepository utilisateurRepository,
                           RatingRepository ratingRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           EntityManager entityManager) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categorieRepository = categorieRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.ratingRepository = ratingRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.entityManager = entityManager;
    }

    /**
     * Récupère l'ID du vendeur actuellement connecté
     */
    private Long getCurrentVendorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String email = authentication.getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        if (!user.getRole().name().equals("VENDEUR")) {
            throw new RuntimeException("Accès réservé aux vendeurs");
        }
        return user.getId();
    }

    /**
     * Récupère le vendeur actuellement connecté
     */
    private Utilisateur getCurrentVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String email = authentication.getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        if (!user.getRole().name().equals("VENDEUR")) {
            throw new RuntimeException("Accès réservé aux vendeurs");
        }
        return user;
    }

    // ========== GESTION DES PRODUITS ==========

    /**
     * Ajouter un nouveau produit
     * POST /api/vendeur/produits
     */
    @Transactional
    @PostMapping("/produits")
    public ResponseEntity<Product> addProduct(@RequestBody ProductRequest request) {
        // Validation
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Le nom du produit est requis");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new RuntimeException("Le prix doit être supérieur à 0");
        }
        if (request.getQuantityAvailable() == null || request.getQuantityAvailable() < 0) {
            throw new RuntimeException("La quantité doit être supérieure ou égale à 0");
        }

        // Récupérer le vendeur connecté
        Utilisateur vendor = getCurrentVendor();

        // Récupérer la catégorie si fournie
        Categorie categorie = null;
        if (request.getCategorieId() != null) {
            categorie = categorieRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
        }

        // Créer le produit
        Product product = Product.builder()
                .asin(request.getAsin() != null ? request.getAsin() : generateAsin())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantityAvailable(request.getQuantityAvailable())
                .categorie(categorie)
                .utilisateur(vendor)
                .rating(0.0)
                .ratingCount(0L)
                .build();

        product = productRepository.save(product);

        // Ajouter les images si fournies
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .displayOrder(i)
                        .build();
                productImageRepository.save(image);
            }
            // Flush to ensure images are persisted before reloading
            entityManager.flush();
            // Clear to force a fresh load from database
            entityManager.clear();
            // Reload product with images eagerly fetched
            product = productRepository.findByIdWithImages(product.getId())
                    .orElse(product);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Modifier un produit existant
     * PUT /api/vendeur/produits/{id}
     */
    @Transactional
    @PutMapping("/produits/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce produit");
        }

        // Mettre à jour les champs
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) {
                throw new RuntimeException("Le prix doit être supérieur à 0");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getQuantityAvailable() != null) {
            if (request.getQuantityAvailable() < 0) {
                throw new RuntimeException("La quantité ne peut pas être négative");
            }
            product.setQuantityAvailable(request.getQuantityAvailable());
        }
        if (request.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            product.setCategorie(categorie);
        }

        // Mettre à jour les images si fournies
        if (request.getImageUrls() != null) {
            // Supprimer les anciennes images
            productImageRepository.deleteByProduct(product);
            entityManager.flush();
            
            // Ajouter les nouvelles images
            if (!request.getImageUrls().isEmpty()) {
                for (int i = 0; i < request.getImageUrls().size(); i++) {
                    String imageUrl = request.getImageUrls().get(i);
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        ProductImage image = ProductImage.builder()
                                .product(product)
                                .imageUrl(imageUrl)
                                .isPrimary(i == 0)
                                .displayOrder(i)
                                .build();
                        productImageRepository.save(image);
                    }
                }
            }
            entityManager.flush();
            entityManager.clear();
            // Recharger le produit avec les images
            product = productRepository.findByIdWithImages(id).orElse(product);
        } else {
            product = productRepository.save(product);
        }

        return ResponseEntity.ok(product);
    }

    /**
     * Supprimer un produit
     * DELETE /api/vendeur/produits/{id}
     */
    @DeleteMapping("/produits/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce produit");
        }

        productRepository.delete(product);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Produit supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Lister tous les produits du vendeur
     * GET /api/vendeur/produits
     */
    @GetMapping("/produits")
    public ResponseEntity<List<Product>> getVendorProducts() {
        List<Product> products = productRepository.findByUtilisateur_Id(getCurrentVendorId());
        return ResponseEntity.ok(products);
    }

    /**
     * Consulter les détails d'un produit
     * GET /api/vendeur/produits/{id}
     */
    @GetMapping("/produits/{id}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à consulter ce produit");
        }

        return ResponseEntity.ok(product);
    }

    // ========== GESTION DES IMAGES ==========

    /**
     * Ajouter des images à un produit
     * POST /api/vendeur/produits/{productId}/images
     */
    @PostMapping("/produits/{productId}/images")
    public ResponseEntity<List<ProductImage>> addProductImages(
            @PathVariable Long productId,
            @RequestBody List<String> imageUrls) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce produit");
        }

        // Récupérer les images existantes pour déterminer l'ordre
        List<ProductImage> existingImages = productImageRepository.findByProduct_IdOrderByDisplayOrderAsc(productId);
        int startOrder = existingImages.size();

        // Ajouter les nouvelles images
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrls.get(i))
                    .isPrimary(existingImages.isEmpty() && i == 0)
                    .displayOrder(startOrder + i)
                    .build();
            productImageRepository.save(image);
        }

        List<ProductImage> allImages = productImageRepository.findByProduct_IdOrderByDisplayOrderAsc(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(allImages);
    }

    /**
     * Supprimer une image d'un produit
     * DELETE /api/vendeur/produits/{productId}/images/{imageId}
     */
    @DeleteMapping("/produits/{productId}/images/{imageId}")
    public ResponseEntity<Map<String, String>> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce produit");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image introuvable"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Cette image n'appartient pas à ce produit");
        }

        productImageRepository.delete(image);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Image supprimée avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Consulter les images d'un produit
     * GET /api/vendeur/produits/{productId}/images
     */
    @GetMapping("/produits/{productId}/images")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à consulter ce produit");
        }

        List<ProductImage> images = productImageRepository.findByProduct_IdOrderByDisplayOrderAsc(productId);
        return ResponseEntity.ok(images);
    }

    // ========== AVIS CLIENTS ==========

    /**
     * Consulter les avis d'un produit
     * GET /api/vendeur/produits/{productId}/reviews
     */
    @GetMapping("/produits/{productId}/reviews")
    public ResponseEntity<List<Rating>> getProductReviews(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à consulter les avis de ce produit");
        }

        List<Rating> reviews = ratingRepository.findByProduit(product);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Consulter les statistiques des avis d'un produit
     * GET /api/vendeur/produits/{productId}/stats
     */
    @GetMapping("/produits/{productId}/stats")
    public ResponseEntity<ProductStatsResponse> getProductStats(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier que le produit appartient au vendeur connecté
        if (!product.getUtilisateur().getId().equals(getCurrentVendorId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à consulter les statistiques de ce produit");
        }

        List<Rating> reviews = ratingRepository.findByProduit(product);

        // Calculer les statistiques
        long totalReviews = reviews.size();
        double averageRating = reviews.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);

        long fiveStar = reviews.stream().filter(r -> r.getStars() == 5).count();
        long fourStar = reviews.stream().filter(r -> r.getStars() == 4).count();
        long threeStar = reviews.stream().filter(r -> r.getStars() == 3).count();
        long twoStar = reviews.stream().filter(r -> r.getStars() == 2).count();
        long oneStar = reviews.stream().filter(r -> r.getStars() == 1).count();

        ProductStatsResponse stats = ProductStatsResponse.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .totalReviews(totalReviews)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .fiveStarCount(fiveStar)
                .fourStarCount(fourStar)
                .threeStarCount(threeStar)
                .twoStarCount(twoStar)
                .oneStarCount(oneStar)
                .build();

        return ResponseEntity.ok(stats);
    }

    // ========== GESTION DES VENTES ==========

    /**
     * Récupérer toutes les ventes du vendeur
     * GET /api/vendeur/ventes
     */
    @GetMapping("/ventes")
    public ResponseEntity<List<Map<String, Object>>> getVendorSales() {
        Long vendorId = getCurrentVendorId();
        // Récupérer tous les produits du vendeur
        List<Product> vendorProducts = productRepository.findByUtilisateur_Id(vendorId);
        
        if (vendorProducts.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Récupérer toutes les commandes contenant les produits du vendeur
        List<OrderItem> vendorOrderItems = orderItemRepository.findByProduct_Utilisateur_Id(vendorId);

        // Créer une liste de ventes avec les détails
        List<Map<String, Object>> sales = vendorOrderItems.stream()
                .map(orderItem -> {
                    Map<String, Object> sale = new HashMap<>();
                    sale.put("id", orderItem.getId());
                    sale.put("orderId", orderItem.getOrder().getId());
                    sale.put("productName", orderItem.getProduct().getTitle());
                    sale.put("productId", orderItem.getProduct().getId());
                    sale.put("quantity", orderItem.getQuantity());
                    sale.put("unitPrice", orderItem.getUnitPrice());
                    sale.put("totalPrice", orderItem.getQuantity() * orderItem.getUnitPrice());
                    sale.put("orderDate", orderItem.getOrder().getCreatedAt());
                    sale.put("buyerName", orderItem.getOrder().getUtilisateur().getNom());
                    return sale;
                })
                .toList();

        return ResponseEntity.ok(sales);
    }

    /**
     * Récupérer les statistiques de ventes du vendeur
     * GET /api/vendeur/ventes/stats
     * GET /api/vendeur/ventes/stats?period=day|month|year
     */
    @GetMapping("/ventes/stats")
    public ResponseEntity<Map<String, Object>> getVendorSalesStats(
            @RequestParam(required = false, defaultValue = "all") String period) {
        
        // Récupérer tous les items de commande pour les produits du vendeur
        List<OrderItem> vendorOrderItems = orderItemRepository.findByProduct_Utilisateur_Id(getCurrentVendorId());

        // Filtrer par période si spécifiée
        if (!period.equals("all")) {
            java.time.LocalDateTime filterDate = java.time.LocalDateTime.now();
            
            switch (period.toLowerCase()) {
                case "day":
                case "today":
                    filterDate = filterDate.minusDays(1);
                    break;
                case "week":
                    filterDate = filterDate.minusWeeks(1);
                    break;
                case "month":
                    filterDate = filterDate.minusMonths(1);
                    break;
                case "year":
                    filterDate = filterDate.minusYears(1);
                    break;
            }
            
            final java.time.LocalDateTime finalFilterDate = filterDate;
            vendorOrderItems = vendorOrderItems.stream()
                    .filter(item -> item.getOrder().getCreatedAt().isAfter(finalFilterDate))
                    .toList();
        }

        // Calculer les statistiques
        int totalOrders = (int) vendorOrderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .count();

        int totalProductsSold = vendorOrderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        double totalRevenue = vendorOrderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        // Top 5 produits les plus vendus
        Map<Long, Integer> productSales = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();
        Map<Long, Double> productRevenue = new HashMap<>();
        for (OrderItem item : vendorOrderItems) {
            Long productId = item.getProduct().getId();
            productSales.put(productId, productSales.getOrDefault(productId, 0) + item.getQuantity());
            productNames.put(productId, item.getProduct().getTitle());
            productRevenue.put(productId, 
                productRevenue.getOrDefault(productId, 0.0) + (item.getQuantity() * item.getUnitPrice()));
        }

        List<Map<String, Object>> topProducts = productSales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", entry.getKey());
                    product.put("productName", productNames.get(entry.getKey()));
                    product.put("quantitySold", entry.getValue());
                    product.put("revenue", productRevenue.get(entry.getKey()));
                    return product;
                })
                .toList();

        // Créer la réponse
        Map<String, Object> stats = new HashMap<>();
        stats.put("period", period);
        stats.put("totalOrders", totalOrders);
        stats.put("totalProductsSold", totalProductsSold);
        stats.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        stats.put("averageOrderValue", Math.round(averageOrderValue * 100.0) / 100.0);
        stats.put("topProducts", topProducts);

        return ResponseEntity.ok(stats);
    }
    
    /**
     * Récupérer les statistiques de ventes par période (jour/mois/année)
     * GET /api/vendeur/dashboard/revenue-by-period?period=day|month|year&limit=30
     */
    @GetMapping("/dashboard/revenue-by-period")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByPeriod(
            @RequestParam(required = false, defaultValue = "day") String period,
            @RequestParam(required = false, defaultValue = "30") int limit) {
        
        List<OrderItem> vendorOrderItems = orderItemRepository.findByProduct_Utilisateur_Id(getCurrentVendorId());
        
        // Grouper par période
        Map<String, Double> revenueByPeriod = new java.util.LinkedHashMap<>();
        Map<String, Integer> ordersByPeriod = new java.util.LinkedHashMap<>();
        
        java.time.format.DateTimeFormatter formatter;
        switch (period.toLowerCase()) {
            case "day":
                formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "month":
                formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "year":
                formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy");
                break;
            default:
                formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
        
        for (OrderItem item : vendorOrderItems) {
            String periodKey = item.getOrder().getCreatedAt().format(formatter);
            double revenue = item.getQuantity() * item.getUnitPrice();
            revenueByPeriod.put(periodKey, revenueByPeriod.getOrDefault(periodKey, 0.0) + revenue);
            ordersByPeriod.put(periodKey, ordersByPeriod.getOrDefault(periodKey, 0) + 1);
        }
        
        // Convertir en liste et trier par date décroissante
        List<Map<String, Object>> result = revenueByPeriod.entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("period", entry.getKey());
                    data.put("revenue", Math.round(entry.getValue() * 100.0) / 100.0);
                    data.put("orders", ordersByPeriod.get(entry.getKey()));
                    return data;
                })
                .toList();
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Récupérer le tableau de bord complet du vendeur
     * GET /api/vendeur/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getVendorDashboard() {
        Long vendorId = getCurrentVendorId();
        List<OrderItem> allVendorOrderItems = orderItemRepository.findByProduct_Utilisateur_Id(vendorId);
        List<Product> vendorProducts = productRepository.findByUtilisateur_Id(vendorId);
        
        // Statistiques générales
        int totalProducts = vendorProducts.size();
        int activeProducts = (int) vendorProducts.stream()
                .filter(p -> p.getQuantityAvailable() != null && p.getQuantityAvailable() > 0)
                .count();
        int outOfStock = totalProducts - activeProducts;
        
        // Statistiques de ventes totales
        int totalOrders = (int) allVendorOrderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .count();
        
        double totalRevenue = allVendorOrderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Statistiques du mois en cours
        java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
        
        List<OrderItem> monthOrderItems = allVendorOrderItems.stream()
                .filter(item -> item.getOrder().getCreatedAt().isAfter(startOfMonth))
                .toList();
        
        int monthOrders = (int) monthOrderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .count();
        
        double monthRevenue = monthOrderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Statistiques d'aujourd'hui
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
        
        List<OrderItem> todayOrderItems = allVendorOrderItems.stream()
                .filter(item -> item.getOrder().getCreatedAt().isAfter(startOfDay))
                .toList();
        
        int todayOrders = (int) todayOrderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .count();
        
        double todayRevenue = todayOrderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Produit le plus vendu (all time)
        Map<Long, Integer> productSales = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();
        for (OrderItem item : allVendorOrderItems) {
            Long productId = item.getProduct().getId();
            productSales.put(productId, productSales.getOrDefault(productId, 0) + item.getQuantity());
            productNames.put(productId, item.getProduct().getTitle());
        }
        
        Map<String, Object> topSellingProduct = null;
        if (!productSales.isEmpty()) {
            var topEntry = productSales.entrySet().stream()
                    .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                    .orElse(null);
            if (topEntry != null) {
                topSellingProduct = new HashMap<>();
                topSellingProduct.put("productId", topEntry.getKey());
                topSellingProduct.put("productName", productNames.get(topEntry.getKey()));
                topSellingProduct.put("totalSold", topEntry.getValue());
            }
        }
        
        // Évaluation moyenne des produits
        double averageRating = vendorProducts.stream()
                .filter(p -> p.getRating() != null && p.getRating() > 0)
                .mapToDouble(Product::getRating)
                .average()
                .orElse(0.0);
        
        // Créer la réponse du tableau de bord
        Map<String, Object> dashboard = new HashMap<>();
        
        // Inventaire
        Map<String, Object> inventory = new HashMap<>();
        inventory.put("totalProducts", totalProducts);
        inventory.put("activeProducts", activeProducts);
        inventory.put("outOfStock", outOfStock);
        dashboard.put("inventory", inventory);
        
        // Ventes totales
        Map<String, Object> totalSales = new HashMap<>();
        totalSales.put("totalOrders", totalOrders);
        totalSales.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        dashboard.put("totalSales", totalSales);
        
        // Ventes du mois
        Map<String, Object> monthlySales = new HashMap<>();
        monthlySales.put("orders", monthOrders);
        monthlySales.put("revenue", Math.round(monthRevenue * 100.0) / 100.0);
        dashboard.put("monthlySales", monthlySales);
        
        // Ventes d'aujourd'hui
        Map<String, Object> dailySales = new HashMap<>();
        dailySales.put("orders", todayOrders);
        dailySales.put("revenue", Math.round(todayRevenue * 100.0) / 100.0);
        dashboard.put("dailySales", dailySales);
        
        // Performance
        Map<String, Object> performance = new HashMap<>();
        performance.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        performance.put("topSellingProduct", topSellingProduct);
        dashboard.put("performance", performance);
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Récupérer les produits avec faible stock
     * GET /api/vendeur/dashboard/low-stock?threshold=10
     */
    @GetMapping("/dashboard/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStockProducts(
            @RequestParam(required = false, defaultValue = "10") int threshold) {
        
        List<Product> vendorProducts = productRepository.findByUtilisateur_Id(getCurrentVendorId());
        
        List<Map<String, Object>> lowStockProducts = vendorProducts.stream()
                .filter(p -> p.getQuantityAvailable() != null && p.getQuantityAvailable() <= threshold && p.getQuantityAvailable() > 0)
                .map(product -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("productId", product.getId());
                    data.put("productName", product.getTitle());
                    data.put("quantityAvailable", product.getQuantityAvailable());
                    data.put("price", product.getPrice());
                    return data;
                })
                .toList();
        
        return ResponseEntity.ok(lowStockProducts);
    }
    
    /**
     * Récupérer les meilleurs clients du vendeur
     * GET /api/vendeur/dashboard/top-customers?limit=10
     */
    @GetMapping("/dashboard/top-customers")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomers(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        List<OrderItem> vendorOrderItems = orderItemRepository.findByProduct_Utilisateur_Id(getCurrentVendorId());
        
        // Grouper par client
        Map<Long, Double> customerRevenue = new HashMap<>();
        Map<Long, String> customerNames = new HashMap<>();
        Map<Long, Integer> customerOrders = new HashMap<>();
        
        for (OrderItem item : vendorOrderItems) {
            Long customerId = item.getOrder().getUtilisateur().getId();
            double revenue = item.getQuantity() * item.getUnitPrice();
            
            customerRevenue.put(customerId, customerRevenue.getOrDefault(customerId, 0.0) + revenue);
            customerNames.put(customerId, item.getOrder().getUtilisateur().getNom());
            customerOrders.put(customerId, customerOrders.getOrDefault(customerId, 0) + 1);
        }
        
        List<Map<String, Object>> topCustomers = customerRevenue.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerId", entry.getKey());
                    customer.put("customerName", customerNames.get(entry.getKey()));
                    customer.put("totalRevenue", Math.round(entry.getValue() * 100.0) / 100.0);
                    customer.put("orderCount", customerOrders.get(entry.getKey()));
                    return customer;
                })
                .toList();
        
        return ResponseEntity.ok(topCustomers);
    }

    /**
     * Récupérer le top 10 des produits par chiffre d'affaires
     * GET /api/vendeur/dashboard/top-products-by-revenue?limit=10
     */
    @GetMapping("/dashboard/top-products-by-revenue")
    public ResponseEntity<List<Map<String, Object>>> getTopProductsByRevenue(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        List<OrderItem> vendorOrderItems = orderItemRepository.findByProduct_Utilisateur_Id(getCurrentVendorId());
        
        // Grouper par produit et calculer le chiffre d'affaires
        Map<Long, Double> productRevenue = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();
        Map<Long, Integer> productQuantitySold = new HashMap<>();
        Map<Long, Double> productPrice = new HashMap<>();
        
        for (OrderItem item : vendorOrderItems) {
            Long productId = item.getProduct().getId();
            double revenue = item.getQuantity() * item.getUnitPrice();
            
            productRevenue.put(productId, productRevenue.getOrDefault(productId, 0.0) + revenue);
            productNames.put(productId, item.getProduct().getTitle());
            productQuantitySold.put(productId, productQuantitySold.getOrDefault(productId, 0) + item.getQuantity());
            productPrice.put(productId, item.getProduct().getPrice());
        }
        
        // Trier par chiffre d'affaires décroissant et limiter au top N
        List<Map<String, Object>> topProducts = new java.util.ArrayList<>();
        java.util.List<Map.Entry<Long, Double>> sortedEntries = productRevenue.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .toList();
        
        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<Long, Double> entry = sortedEntries.get(i);
            Map<String, Object> product = new HashMap<>();
            product.put("rank", i + 1);
            product.put("productId", entry.getKey());
            product.put("productName", productNames.get(entry.getKey()));
            product.put("totalRevenue", Math.round(entry.getValue() * 100.0) / 100.0);
            product.put("quantitySold", productQuantitySold.get(entry.getKey()));
            product.put("currentPrice", productPrice.get(entry.getKey()));
            product.put("averagePrice", Math.round((entry.getValue() / productQuantitySold.get(entry.getKey())) * 100.0) / 100.0);
            topProducts.add(product);
        }
        
        return ResponseEntity.ok(topProducts);
    }

    /**
     * Récupérer les détails d'une vente spécifique
     * GET /api/vendeur/ventes/{orderId}
     */
    @GetMapping("/ventes/{orderId}")
    public ResponseEntity<Map<String, Object>> getVendorSaleDetails(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        // Récupérer uniquement les items du vendeur dans cette commande
        List<OrderItem> vendorItems = order.getItems().stream()
                .filter(item -> item.getProduct().getUtilisateur().getId().equals(getCurrentVendorId()))
                .toList();

        if (vendorItems.isEmpty()) {
            throw new RuntimeException("Aucun produit de ce vendeur dans cette commande");
        }

        // Calculer le total pour le vendeur
        double vendorTotal = vendorItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        // Créer la réponse
        Map<String, Object> saleDetails = new HashMap<>();
        saleDetails.put("orderId", order.getId());
        saleDetails.put("orderDate", order.getCreatedAt());
        saleDetails.put("buyerName", order.getUtilisateur().getNom());
        saleDetails.put("vendorTotal", vendorTotal);
        saleDetails.put("items", vendorItems.stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productName", item.getProduct().getTitle());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("totalPrice", item.getQuantity() * item.getUnitPrice());
                    return itemMap;
                })
                .toList());

        return ResponseEntity.ok(saleDetails);
    }

    // Méthode utilitaire pour générer un code ASIN unique
    private String generateAsin() {
        return "VEND" + System.currentTimeMillis();
    }
}
