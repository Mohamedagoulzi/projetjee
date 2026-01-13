package org.example.projectjee.repository;

import org.example.projectjee.model.CartItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query(value = """
        SELECT
          ap.utilisateur_id,
          ap.utilisateur_id,
          u.nom,
          NULL,
          COALESCE(SUM(ap.quantite * p.prix), 0) AS total
        FROM articles_panier ap
        JOIN produits p ON p.id = ap.produit_id
        JOIN utilisateur u ON u.id = ap.utilisateur_id
        GROUP BY ap.utilisateur_id, u.nom
        ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> findPendingCartsRaw();

    @Query(value = "SELECT COUNT(DISTINCT utilisateur_id) FROM articles_panier", nativeQuery = true)
    long countDistinctUsersHavingCartNative();

    @Query(value = """
        SELECT COALESCE(SUM(ap.quantite * p.prix), 0)
        FROM articles_panier ap
        JOIN produits p ON p.id = ap.produit_id
    """, nativeQuery = true)
    double sumCartValueNative();

    List<CartItem> findByUtilisateur_Id(Long utilisateurId);

        // ✅ AJOUTEZ CETTE MÉTHODE
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.product.id = :productId")
    void deleteByProduct_Id(@Param("productId") Long productId);
}
