package org.example.projectjee.dto;

import java.time.LocalDateTime;

public class AdminUserResponseDTO {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private LocalDateTime dateCreation;

    public AdminUserResponseDTO(Long id, String nom, String email, String role, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.dateCreation = dateCreation;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
