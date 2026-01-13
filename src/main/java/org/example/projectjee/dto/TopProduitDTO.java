package org.example.projectjee.dto;

import java.math.BigDecimal;

public class TopProduitDTO {

    private Long id;
    private String nom;
    private String categorieNom;
    private BigDecimal prix;
    private BigDecimal noteMoyenne;
    private Integer nombreReviews;

    public TopProduitDTO() {
    }

    // ✅ Constructeur EXACT possible si price/rating sont BigDecimal et ratingCount Integer
    public TopProduitDTO(Long id, String nom, String categorieNom,
                         BigDecimal prix, BigDecimal noteMoyenne, Integer nombreReviews) {
        this.id = id;
        this.nom = nom;
        this.categorieNom = categorieNom;
        this.prix = prix;
        this.noteMoyenne = noteMoyenne;
        this.nombreReviews = nombreReviews;
    }

    // ✅ Très fréquent: price/rating en Double (MySQL) + ratingCount Integer
    public TopProduitDTO(Long id, String nom, String categorieNom,
                         Double prix, Double noteMoyenne, Integer nombreReviews) {
        this(id, nom, categorieNom,
                prix == null ? null : BigDecimal.valueOf(prix),
                noteMoyenne == null ? null : BigDecimal.valueOf(noteMoyenne),
                nombreReviews);
    }

    // ✅ Variant si ratingCount est Long
    public TopProduitDTO(Long id, String nom, String categorieNom,
                         Double prix, Double noteMoyenne, Long nombreReviews) {
        this(id, nom, categorieNom,
                prix == null ? null : BigDecimal.valueOf(prix),
                noteMoyenne == null ? null : BigDecimal.valueOf(noteMoyenne),
                nombreReviews == null ? null : nombreReviews.intValue());
    }

    // ✅ Getters / Setters (sans Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorieNom() { return categorieNom; }
    public void setCategorieNom(String categorieNom) { this.categorieNom = categorieNom; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public BigDecimal getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(BigDecimal noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public Integer getNombreReviews() { return nombreReviews; }
    public void setNombreReviews(Integer nombreReviews) { this.nombreReviews = nombreReviews; }
}
