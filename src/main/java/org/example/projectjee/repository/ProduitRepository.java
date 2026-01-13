package org.example.projectjee.repository;

import java.util.List;

import org.example.projectjee.dto.ProduitCategorieStatDTO;
import org.example.projectjee.dto.TopProduitDTO;
import org.example.projectjee.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ⭐ Moyenne globale des notes
    @Query("SELECT COALESCE(AVG(p.rating), 0) FROM Product p")
    Double findNoteMoyenneGlobale();

    // ⭐ Top 10 produits (ex: les plus chers)
    List<Product> findTop10ByOrderByPriceDesc();

    // ⭐ Top 10 produits pour une catégorie donnée
    // categorie est un ManyToOne, donc on utilise categorie.id
    List<Product> findTop10ByCategorie_IdOrderByPriceDesc(Long categorieId);

    // ⭐ Nombre de produits par catégorie (stat)
    @Query("""
            SELECT new org.example.projectjee.dto.ProduitCategorieStatDTO(
                c.nom,
                COUNT(p)
            )
            FROM Product p
            JOIN p.categorie c
            GROUP BY c.nom
            """)
    List<ProduitCategorieStatDTO> countProduitsParCategorie();

    // ✅ KPI 1 : best rated (note desc, puis nombre d’avis desc)
    List<Product> findByOrderByRatingDescRatingCountDesc(Pageable pageable);

    // ✅ KPI 2 : most "sold" (proxy = nombre d’avis)
    List<Product> findByOrderByRatingCountDesc(Pageable pageable);

    // ✅ KPI 3 : distribution des notes (arrondies 1..5)
    @Query("""
            select function('round', p.rating) as stars, count(p.id) as cnt
            from Product p
            where p.rating is not null
            group by function('round', p.rating)
            order by stars desc
            """)
    List<Object[]> ratingDistribution();

    // ⭐ Top 10 produits (par nombre de reviews / ventes)
    @Query("""
            SELECT new org.example.projectjee.dto.TopProduitDTO(
                p.id,
                p.title,
                c.nom,
                p.price,
                p.rating,
                p.ratingCount
            )
            FROM Product p
            LEFT JOIN p.categorie c
            ORDER BY p.ratingCount DESC
            """)
    List<TopProduitDTO> findTop10DTO(Pageable pageable);

    // ✅ Méthodes ajoutées depuis ProductRepository pour compatibilité
    // Find products by vendor/seller
    List<Product> findByUtilisateur_Id(Long vendorId);

    // Search by title (keyword search)
    List<Product> findByTitleContainingIgnoreCase(String keyword);

    // Find by ASIN
    Optional<Product> findByAsin(String asin);

    // Find by category
    List<Product> findByCategorie_Id(Long categoryId);

    // Find by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Find by rating greater than
    List<Product> findByRatingGreaterThanEqual(Double minRating);

    // Advanced search with multiple filters
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            +
            "(:categoryId IS NULL OR p.categorie.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:minRating IS NULL OR p.rating >= :minRating)")
    List<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating);

    // Find product by ID with images eagerly loaded
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);
}
