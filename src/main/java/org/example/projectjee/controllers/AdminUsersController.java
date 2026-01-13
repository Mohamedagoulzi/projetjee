package org.example.projectjee.controllers;

import java.util.List;

import org.example.projectjee.dto.AdminUserCreateDTO;
import org.example.projectjee.dto.AdminUserResponseDTO;
import org.example.projectjee.dto.AdminUserUpdateDTO;
import org.example.projectjee.services.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminUsersController {

    private final AdminUserService adminUserService;

    public AdminUsersController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserResponseDTO> all() {
        return adminUserService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<AdminUserResponseDTO> create(@RequestBody AdminUserCreateDTO dto) {
        return ResponseEntity.ok(adminUserService.createUser(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponseDTO> update(@PathVariable Long id,
                                                       @RequestBody AdminUserUpdateDTO dto) {
        return ResponseEntity.ok(adminUserService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
