package org.example.projectjee.dto;

public class AdminUserCreateDTO {
    private String nom;
    private String email;
    private String motDePasse;
    private String role; // "ADMIN" | "ACHETEUR" | "VENDEUR" | "ANALYSTE"

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
