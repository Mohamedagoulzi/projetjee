package org.example.projectjee.config;

import org.springframework.beans.factory.annotation.Autowired; // ✅ IMPORTANT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthFilter; // ✅ ton filtre JWT (annoté @Component)

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                // (optionnel mais conseillé)
                                .cors(cors -> {
                                }) // si tu veux gérer le CORS proprement plus tard

                                // 🔐 Autoriser les endpoints publics
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/register",
                                                                "/*.png",
                                                                "/api/auth/reset-password",
                                                                "/api/utilisateur/**",
                                                                "/api/utilisateur/topCategorie",
                                                                "/api/admin/**",
                                                                "/api/admin/export/**",
                                                                "/api/utilisateur/export/**",
                                                                "/api/products/**",
                                                                "/api/produits/**", // ✅ Pour la synchro Python
                                                                "/api/vendeur/**")
                                                .permitAll()
                                                // Tous les autres endpoints nécessitent un JWT
                                                .anyRequest().authenticated())

                                // 🟡 Désactiver les sessions (JWT = stateless)
                                .sessionManagement(sm -> sm
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 🟢 Ajouter le filtre JWT AVANT le filtre d’auth standard
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }
}
