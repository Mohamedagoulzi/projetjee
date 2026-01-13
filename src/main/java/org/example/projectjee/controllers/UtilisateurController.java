package org.example.projectjee.controllers;

import org.example.projectjee.dto.UpdateUserRequest;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateur")
@CrossOrigin(origins = "http://localhost:3000")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // UPDATE PROFILE
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UpdateUserRequest request) {
        String email = authentication.getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getNom() != null && !request.getNom().isEmpty()) {
            user.setNom(request.getNom());
        }

        // Si l'utilisateur change son email, on vérifie s'il n'est pas déjà pris par
        // quelqu'un d'autre
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && !request.getEmail().equals(user.getEmail())) {
            if (utilisateurRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Cet email est déjà utilisé.");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        }

        utilisateurRepository.save(user);

        return ResponseEntity.ok("Profil mis à jour avec succès");
    }
}
