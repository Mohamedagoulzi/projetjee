package org.example.projectjee.spec;

import java.math.BigDecimal;

import org.example.projectjee.model.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProduitSpecifications {

    public static Specification<Product> withFilters(
            String q,
            Long categorieId,
            BigDecimal prixMin,
            BigDecimal prixMax,
            BigDecimal noteMin,
            BigDecimal noteMax,
            Integer reviewsMin,
            Integer reviewsMax) {
        return (root, query, cb) -> {
            var p = cb.conjunction();

            // 🔎 search q dans title + description
            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p = cb.and(p, cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }

            // 🔹 filtre catégorie
            if (categorieId != null) {
                p = cb.and(p, cb.equal(root.get("categorie").get("id"), categorieId));
            }

            // 💰 prix : champ = price (Double)
            if (prixMin != null) {
                p = cb.and(p,
                        cb.greaterThanOrEqualTo(root.get("price"),
                                prixMin.doubleValue()));
            }
            if (prixMax != null) {
                p = cb.and(p,
                        cb.lessThanOrEqualTo(root.get("price"),
                                prixMax.doubleValue()));
            }

            // ⭐ note : champ = rating (Double)
            if (noteMin != null) {
                p = cb.and(p,
                        cb.greaterThanOrEqualTo(root.get("rating"),
                                noteMin.doubleValue()));
            }
            if (noteMax != null) {
                p = cb.and(p,
                        cb.lessThanOrEqualTo(root.get("rating"),
                                noteMax.doubleValue()));
            }

            // 📝 reviews : champ = ratingCount (Long)
            if (reviewsMin != null) {
                p = cb.and(p,
                        cb.greaterThanOrEqualTo(root.get("ratingCount"),
                                reviewsMin.longValue()));
            }
            if (reviewsMax != null) {
                p = cb.and(p,
                        cb.lessThanOrEqualTo(root.get("ratingCount"),
                                reviewsMax.longValue()));
            }

            return p;
        };
    }
}
