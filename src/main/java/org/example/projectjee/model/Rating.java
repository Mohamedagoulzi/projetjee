package org.example.projectjee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis_produits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  Un utilisateur peut faire plusieurs avis
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    //  Plusieurs avis concernent un produit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id")
    private Product produit;

    @Column(name = "nb_etoiles")
    private Integer stars;   // 1 à 5

    @Column(name = "commentaire")
    private String comment;

    @Column(name = "date_creation")
    private LocalDateTime createdAt;
}
