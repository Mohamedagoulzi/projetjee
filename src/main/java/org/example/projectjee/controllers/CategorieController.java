package org.example.projectjee.controllers;

import java.util.List;

import org.example.projectjee.model.Categorie;
import org.example.projectjee.repository.CategorieRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategorieController {

    private final CategorieRepository categorieRepository;

    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    // 1) Liste de toutes les catégories
    @GetMapping
    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    // 2) Détail d'une catégorie par id
    @GetMapping("/{id}")
    public Categorie getCategoryById(@PathVariable Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
