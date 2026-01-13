package org.example.projectjee.dto;

public class TopCategorieDTO {
    private String categorieNom;
    private long totalQte;

    public TopCategorieDTO(String categorieNom, long totalQte) {
        this.categorieNom = categorieNom;
        this.totalQte = totalQte;
    }

    public String getCategorieNom() { return categorieNom; }
    public long getTotalQte() { return totalQte; }
}
