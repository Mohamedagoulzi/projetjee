package org.example.projectjee.controllers;

import org.example.projectjee.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/analyst")
@CrossOrigin(origins = "http://localhost:3000")
public class AnalystController {

    private final OrderRepository orderRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public AnalystController(OrderRepository orderRepository,
                            UtilisateurRepository utilisateurRepository,
                            ProduitRepository produitRepository,
                            CategorieRepository categorieRepository) {
        this.orderRepository = orderRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
    }

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs() {
        Map<String, Object> kpis = new HashMap<>();
        
        try {
            kpis.put("totalRevenue", orderRepository.sumOrdersTotalNative());
            kpis.put("totalOrders", orderRepository.countOrdersNative());
            kpis.put("totalUsers", utilisateurRepository.count());
            kpis.put("totalProducts", produitRepository.count());
            kpis.put("avgOrderValue", calculateAvgOrderValue());
            kpis.put("conversionRate", 3.2); // À calculer selon votre logique
            kpis.put("growthRate", 12.5); // À calculer selon votre logique
            kpis.put("returningCustomers", 45.8); // À calculer selon votre logique
        } catch (Exception e) {
            // Valeurs par défaut en cas d'erreur
            kpis.put("totalRevenue", 0);
            kpis.put("totalOrders", 0);
            kpis.put("totalUsers", 0);
            kpis.put("totalProducts", 0);
            kpis.put("avgOrderValue", 0);
            kpis.put("conversionRate", 0);
            kpis.put("growthRate", 0);
            kpis.put("returningCustomers", 0);
        }
        
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/sales")
    public ResponseEntity<List<Map<String, Object>>> getSalesData(@RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> salesData = new ArrayList<>();
        
        // Générer des données pour les X derniers jours
        for (int i = days; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toLocalDate().toString());
            dayData.put("revenue", Math.random() * 5000 + 2000);
            dayData.put("orders", (int)(Math.random() * 50) + 20);
            dayData.put("profit", Math.random() * 1500 + 500);
            salesData.add(dayData);
        }
        
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategoryData() {
        List<Map<String, Object>> categories = new ArrayList<>();
        
        categorieRepository.findAll().forEach(cat -> {
            Map<String, Object> catData = new HashMap<>();
            catData.put("name", cat.getNom());
            catData.put("value", (int)(Math.random() * 30) + 10);
            catData.put("revenue", Math.random() * 50000 + 10000);
            categories.add(catData);
        });
        
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts() {
        List<Map<String, Object>> products = new ArrayList<>();
        
        produitRepository.findAll().stream().limit(10).forEach(prod -> {
            Map<String, Object> prodData = new HashMap<>();
            prodData.put("id", prod.getId());
            prodData.put("name", prod.getTitle());
            prodData.put("sales", (int)(Math.random() * 300) + 50);
            prodData.put("revenue", prod.getPrice() * ((int)(Math.random() * 100) + 20));
            prodData.put("growth", Math.random() * 30 - 5);
            products.add(prodData);
        });
        
        return ResponseEntity.ok(products);
    }

    @GetMapping("/top-sellers")
    public ResponseEntity<List<Map<String, Object>>> getTopSellers() {
        List<Map<String, Object>> sellers = new ArrayList<>();
        
        utilisateurRepository.findAll().stream()
            .filter(u -> u.getRole().name().equals("VENDEUR"))
            .limit(10)
            .forEach(seller -> {
                Map<String, Object> sellerData = new HashMap<>();
                sellerData.put("id", seller.getId());
                sellerData.put("name", seller.getNom());
                sellerData.put("sales", (int)(Math.random() * 500) + 100);
                sellerData.put("revenue", Math.random() * 100000 + 20000);
                sellerData.put("rating", Math.round((Math.random() * 1 + 4) * 10.0) / 10.0);
                sellers.add(sellerData);
            });
        
        return ResponseEntity.ok(sellers);
    }

    @GetMapping("/user-growth")
    public ResponseEntity<List<Map<String, Object>>> getUserGrowth() {
        List<Map<String, Object>> growth = new ArrayList<>();
        
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        for (String month : months) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("newUsers", (int)(Math.random() * 100) + 50);
            monthData.put("activeUsers", (int)(Math.random() * 300) + 200);
            growth.add(monthData);
        }
        
        return ResponseEntity.ok(growth);
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders() {
        List<Map<String, Object>> orders = new ArrayList<>();
        
        orderRepository.findAll().stream().limit(10).forEach(order -> {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", "ORD-" + String.format("%03d", order.getId()));
            orderData.put("customer", order.getUtilisateur().getNom());
            orderData.put("amount", order.getTotalAmount());
            orderData.put("status", "completed");
            orderData.put("date", order.getCreatedAt().toLocalDate().toString());
            orders.add(orderData);
        });
        
        return ResponseEntity.ok(orders);
    }

    private double calculateAvgOrderValue() {
        long count = orderRepository.countOrdersNative();
        if (count == 0) return 0;
        return orderRepository.sumOrdersTotalNative() / count;
    }
}