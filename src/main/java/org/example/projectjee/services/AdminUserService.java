package org.example.projectjee.services;

import java.util.List;

import org.example.projectjee.dto.AdminUserCreateDTO;
import org.example.projectjee.dto.AdminUserResponseDTO;
import org.example.projectjee.dto.AdminUserUpdateDTO; // ton enum Role
import org.example.projectjee.model.Role;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UtilisateurRepository utilisateurRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserResponseDTO> getAllUsers() {
        return utilisateurRepository.findAll().stream()
                .map(u -> new AdminUserResponseDTO(u.getId(), u.getNom(), u.getEmail(),
                        String.valueOf(u.getRole()), u.getDateCreation()))
                .toList();
    }

    public AdminUserResponseDTO createUser(AdminUserCreateDTO dto) {
        // email unique
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Utilisateur u = new Utilisateur();
        u.setNom(dto.getNom());
        u.setEmail(dto.getEmail());
        u.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        u.setRole(Role.valueOf(dto.getRole())); // doit matcher ton enum
        // dateCreation si tu la gères via @PrePersist c’est OK

        Utilisateur saved = utilisateurRepository.save(u);
        return new AdminUserResponseDTO(saved.getId(), saved.getNom(), saved.getEmail(),
                String.valueOf(saved.getRole()), saved.getDateCreation());
    }

    public AdminUserResponseDTO updateUser(Long id, AdminUserUpdateDTO dto) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // si email change -> vérifier unique
        if (dto.getEmail() != null && !dto.getEmail().equals(u.getEmail())) {
            if (utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email déjà utilisé");
            }
            u.setEmail(dto.getEmail());
        }

        if (dto.getNom() != null) u.setNom(dto.getNom());
        if (dto.getRole() != null) u.setRole(Role.valueOf(dto.getRole()));

        Utilisateur saved = utilisateurRepository.save(u);
        return new AdminUserResponseDTO(saved.getId(), saved.getNom(), saved.getEmail(),
                String.valueOf(saved.getRole()), saved.getDateCreation());
    }

    public void deleteUser(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        utilisateurRepository.deleteById(id);
    }
}
