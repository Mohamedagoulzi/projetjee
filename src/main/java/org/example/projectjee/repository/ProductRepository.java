package org.example.projectjee.repository;

import org.example.projectjee.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

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
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.categorie.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating)")
    List<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating
    );

    // Find product by ID with images eagerly loaded
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);
}