package org.example.projectjee.repository;

import java.util.List;

import org.example.projectjee.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenteRepository extends JpaRepository<Order, Long> {

    @Query(
        value = """
        SELECT 
            CASE 
              WHEN :periode = 'mois' THEN DATE_FORMAT(v.date_creation, '%Y-%m')
              WHEN :periode = 'semaine' THEN CONCAT(YEAR(v.date_creation), '-W', LPAD(WEEK(v.date_creation, 1), 2, '0'))
              ELSE DATE_FORMAT(v.date_creation, '%Y-%m-%d')
            END AS period_label,
            c.nom AS category_name,
            SUM(l.quantite * l.prix_unitaire) AS total_amount
        FROM commandes v
        JOIN lignes_commande l ON l.commande_id = v.id
        JOIN produits p ON p.id = l.produit_id
        JOIN categorie c ON c.id = p.categorie_id
        GROUP BY period_label, c.nom
        ORDER BY MIN(v.date_creation)
        """,
        nativeQuery = true
    )
    List<Object[]> findSalesGroupedByPeriodAndCategory(
            @Param("periode") String periode
    );



    @Query(value = """
        SELECT c.nom AS categorieNom,
            COALESCE(SUM(lc.quantite), 0) AS totalQte
        FROM lignes_commande lc
        JOIN produits p   ON p.id = lc.produit_id
        JOIN categorie c  ON c.id = p.categorie_id
        GROUP BY c.id, c.nom
        ORDER BY totalQte DESC
        LIMIT 1
    """, nativeQuery = true)
    List<Object[]> findTopCategorie();
}
