package org.example.projectjee.repository;

import org.example.projectjee.model.Order;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUtilisateurId(Long utilisateurId);

    @Query(value = """
        SELECT 
          c.id,
          c.utilisateur_id,
          u.nom,
          c.date_creation,
          c.montant_total
        FROM commandes c
        JOIN utilisateur u ON u.id = c.utilisateur_id
        ORDER BY c.date_creation DESC
    """, nativeQuery = true)
    List<Object[]> findAllAdminRowsNativeRaw();

    @Query(value = "SELECT COUNT(*) FROM commandes", nativeQuery = true)
    long countOrdersNative();

    @Query(value = "SELECT COALESCE(SUM(montant_total),0) FROM commandes", nativeQuery = true)
    double sumOrdersTotalNative();
}
