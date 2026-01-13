package org.example.projectjee.repository;

import org.example.projectjee.model.Rating;
import org.example.projectjee.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByProduit(Product product);

    List<Rating> findByUtilisateur_Id(Long utilisateurId);
}