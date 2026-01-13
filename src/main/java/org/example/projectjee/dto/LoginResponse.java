package org.example.projectjee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectjee.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long id;
    private String nom;
    private String email;
    private Role role;
    private String token;   // ✅ NOUVEAU
}
