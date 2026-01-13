package org.example.projectjee.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String reponseSecrete;
    private String nouveauPassword;
}
