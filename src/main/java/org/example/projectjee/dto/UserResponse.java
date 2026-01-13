package org.example.projectjee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String nom;
    private String email;
    private String role;
}
