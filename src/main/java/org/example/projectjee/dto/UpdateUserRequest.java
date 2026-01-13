package org.example.projectjee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String nom;
    private String email;
    // On peut ajouter mot de passe, adresse, etc. si besoin
    private String password; // Optionnel : nouveau mot de passe
}
