package org.example.projectjee.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_asin", nullable = false, unique = true)
    private String asin;

    @Column(name = "titre")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "prix")
    private Double price;

    @Column(name = "note_moyenne")
    private Double rating;

    @Column(name = "nombre_avis")
    private Long ratingCount;

    @Column(name = "rang_amazon")
    private Integer rank;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "Nombre_vendur")
    private String no_sellers;
        
    @Column(name = "quantite_disponible")
    private Integer quantityAvailable;

    @PreUpdate
    @PrePersist
    private void validateStock() {
        if (quantityAvailable != null && quantityAvailable < 0) {
            throw new IllegalStateException("Stock quantity cannot be negative. Current value: " + quantityAvailable);
        }
    }

    @JsonIgnoreProperties({"product"})
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();
}
