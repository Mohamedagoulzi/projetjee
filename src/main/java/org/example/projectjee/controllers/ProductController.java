package org.example.projectjee.controllers;

import org.example.projectjee.model.Product;
import org.example.projectjee.repository.ProduitRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000") // port Vite par défaut
public class ProductController {

    private final ProduitRepository productRepository;

    public ProductController(ProduitRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 1) Liste de tous les produits
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 2) Détail d'un produit par id
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
    }

    // 3) Recherche par mot-clé (search)
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new RuntimeException("Le mot-clé de recherche ne peut pas être vide");
        }
        return productRepository.findByTitleContainingIgnoreCase(keyword.trim());
    }

    // 4) Filtrage avancé avec plusieurs critères
    @GetMapping("/filter")
    public List<Product> filterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating) {
        return productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, minRating);
    }

    // 5) Recherche par catégorie
    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productRepository.findByCategorie_Id(categoryId);
    }

    // 6) Recherche par ASIN
    @GetMapping("/asin/{asin}")
    public Product getProductByAsin(@PathVariable String asin) {
        return productRepository.findByAsin(asin)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
    }

    // 7) Recherche par plage de prix
    @GetMapping("/price-range")
    public List<Product> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        if (minPrice < 0 || maxPrice < 0) {
            throw new RuntimeException("Les prix ne peuvent pas être négatifs");
        }
        if (minPrice > maxPrice) {
            throw new RuntimeException("Le prix minimum ne peut pas être supérieur au prix maximum");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    // 8) Recherche par note minimale
    @GetMapping("/rating/{minRating}")
    public List<Product> getProductsByRating(@PathVariable Double minRating) {
        if (minRating < 0.0 || minRating > 5.0) {
            throw new RuntimeException("La note doit être entre 0.0 et 5.0");
        }
        return productRepository.findByRatingGreaterThanEqual(minRating);
    }
}
