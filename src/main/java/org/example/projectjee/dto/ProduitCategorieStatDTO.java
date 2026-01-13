package org.example.projectjee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProduitCategorieStatDTO {

    private String categorieNom;   // nom de la catégorie
    private Long totalProduits;    // nb de produits dans cette catégorie
}
