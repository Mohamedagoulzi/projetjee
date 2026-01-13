package org.example.projectjee.controllers;

import lombok.RequiredArgsConstructor;
import org.example.projectjee.dto.*;
import org.example.projectjee.services.AdminCommandesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminCommandesController {

    private final AdminCommandesService adminCommandesService;

    // ✅ 1) KPIs
    @GetMapping("/commandes/kpis")
    public AdminCommandeKpisDTO kpis() {
        return adminCommandesService.getKpis();
    }

    // ✅ 2) Confirmées = commandes
    @GetMapping("/commandes")
    public List<AdminOrderRowDTO> commandes() {
        return adminCommandesService.getConfirmedOrders();
    }

    // ✅ 3) Non confirmées = paniers (agrégés depuis CartItem)
    @GetMapping("/paniers")
    public List<AdminCartRowDTO> paniers() {
        return adminCommandesService.getPendingCarts();
    }
}
