// src/main/java/org/example/projectjee/dto/UserChecklistDTO.java
package org.example.projectjee.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChecklistDTO {

    private Long id;
    private String nom;
    private String email;
    private LocalDateTime dateCreation;
    private String role;   // on convertira l’enum en String
}
