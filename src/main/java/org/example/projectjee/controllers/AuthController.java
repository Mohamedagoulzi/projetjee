package org.example.projectjee.controllers;

import java.util.Optional;

import org.example.projectjee.dto.LoginRequest;
import org.example.projectjee.dto.LoginResponse;
import org.example.projectjee.dto.RegisterRequest;
import org.example.projectjee.dto.ResetPasswordRequest;
import org.example.projectjee.dto.UserResponse;
import org.example.projectjee.model.Role;
import org.example.projectjee.model.Utilisateur;
import org.example.projectjee.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.projectjee.services.JwtService;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // adapte si ton frontend a un autre port
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthController(UtilisateurRepository utilisateurRepository,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
    this.utilisateurRepository = utilisateurRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
}


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // 1) Vérifier si l’email existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Cet email est déjà utilisé.");
        }

        // 2) Créer un utilisateur (par défaut ACHETEUR)
        Utilisateur user = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getPassword()))
                .reponseSecrete(passwordEncoder.encode(request.getReponseSecrete()))
                .role(Role.ACHETEUR)
                .build();

        // 3) Sauvegarder en BD
        Utilisateur saved = utilisateurRepository.save(user);

        // 4) Réponse simple
        return ResponseEntity.ok("Utilisateur créé avec l'id: " + saved.getId());
    }


    // 🔹 LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<Utilisateur> optUser = utilisateurRepository.findByEmail(request.getEmail());

        if (optUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect.");
        }

        Utilisateur user = optUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getMotDePasse())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect.");
        }

        // OK : on renvoie les infos utiles (dont le rôle)
        String token = jwtService.generateToken(user.getEmail());

        LoginResponse response = LoginResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)   // très important
                .build();

        return ResponseEntity.ok(response);

    }

    @PostMapping("/reset-password")
public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {

    Optional<Utilisateur> opt = utilisateurRepository.findByEmail(req.getEmail());
    if (opt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Email ou réponse incorrecte.");
    }

    Utilisateur user = opt.get();

    // Vérifier la réponse secrète
    if (!passwordEncoder.matches(req.getReponseSecrete(), user.getReponseSecrete())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Réponse incorrecte.");
    }

    // Mettre à jour le mot de passe
    user.setMotDePasse(passwordEncoder.encode(req.getNouveauPassword()));
    utilisateurRepository.save(user);

    return ResponseEntity.ok("Mot de passe réinitialisé avec succès !");
}

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

        // email de l'utilisateur connecté (le "username" du token)
        String email = authentication.getName();

        // ⚠️ adapter selon ton repository :
        // si ta méthode retourne Optional<Utilisateur> :
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // si ta méthode retourne directement Utilisateur, alors juste :
        // Utilisateur user = utilisateurRepository.findByEmail(email);

        UserResponse dto = new UserResponse(
                user.getNom(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(dto);
    }



}
