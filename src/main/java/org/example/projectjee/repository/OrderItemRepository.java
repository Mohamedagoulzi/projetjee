package org.example.projectjee.repository;

import org.example.projectjee.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Trouver tous les items de commande pour les produits d'un vendeur spécifique
    List<OrderItem> findByProduct_Utilisateur_Id(Long vendeurId);


    // ✅ AJOUTEZ CETTE MÉTHODE
    @Transactional
    @Modifying
    @Query("UPDATE OrderItem o SET o.product = NULL WHERE o.product.id = :productId")
    void setProductToNullByProductId(@Param("productId") Long productId);


    
}