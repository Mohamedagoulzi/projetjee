package org.example.projectjee.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.projectjee.dto.ProductKpisDTO;
import org.example.projectjee.dto.RatingBucketDTO;
import org.example.projectjee.dto.TopProduitDTO;
import org.example.projectjee.model.Product;
import org.example.projectjee.repository.ProduitRepository;
import org.example.projectjee.spec.ProduitSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProduitKpiService {

    private final ProduitRepository produitRepository;

    // ✅ KPIs AVEC FILTRE
    public ProductKpisDTO getKpis(
            String q,
            Long categorieId,
            BigDecimal prixMin,
            BigDecimal prixMax,
            BigDecimal noteMin,
            BigDecimal noteMax,
            Integer reviewsMin,
            Integer reviewsMax
    ) {

        Specification<Product> spec = ProduitSpecifications.withFilters(
                q, categorieId, prixMin, prixMax, noteMin, noteMax, reviewsMin, reviewsMax
        );

        List<Product> list = produitRepository.findAll(spec);
        long total = list.size();

        // 1️⃣ Produit le mieux noté (note desc, tie -> reviews desc)
        Product bestRated = list.stream()
                .max(Comparator
                        .comparing((Product p) -> bd(p.getRating()))              // rating (Double)
                        .thenComparing(p -> intv(p.getRatingCount()))           // ratingCount (Long)
                )
                .orElse(null);

        // 2️⃣ Produit le plus vendu (proxy = nombre d'avis)
        Product mostSold = list.stream()
                .max(Comparator.comparing(p -> intv(p.getRatingCount())))
                .orElse(null);

        // 3️⃣ Distribution des notes (round -> 1..5)
        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);

        for (Product p : list) {
            if (p.getRating() == null) continue;

            double r = p.getRating();
            int stars = (int) Math.round(r);
            stars = Math.max(1, Math.min(5, stars));

            dist.put(stars, dist.get(stars) + 1);
        }

        List<RatingBucketDTO> buckets = new ArrayList<>();
        for (int s = 5; s >= 1; s--) {
            buckets.add(new RatingBucketDTO(s, dist.get(s)));
        }

        return ProductKpisDTO.builder()
                .bestRated(toTopProduitDTO(bestRated))
                .mostSold(toTopProduitDTO(mostSold))
                .distribution(buckets)
                .totalProduits(total)
                .build();
    }

    // (Optionnel) ancien comportement sans filtres
    public ProductKpisDTO getKpis() {
        return getKpis(null, null, null, null, null, null, null, null);
    }

    // 🔁 Mapping EXACT vers ton DTO existant
    private TopProduitDTO toTopProduitDTO(Product p) {
        if (p == null) return null;

        return new TopProduitDTO(
                p.getId(),
                p.getTitle(),                                                   // ancien nom -> title
                p.getCategorie() != null ? p.getCategorie().getNom() : null,
                p.getPrice() != null ? BigDecimal.valueOf(p.getPrice()) : null,
                p.getRating() != null ? BigDecimal.valueOf(p.getRating()) : null,
                p.getRatingCount() != null ? p.getRatingCount().intValue() : null
        );
    }

    // Helpers null-safe adaptés aux types de Product
    private Double bd(Double x) { return x == null ? 0.0 : x; }

    private int intv(Long x) { return x == null ? 0 : x.intValue(); }



    
}
