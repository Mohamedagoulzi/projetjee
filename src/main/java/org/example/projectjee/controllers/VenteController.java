package org.example.projectjee.controllers;

import org.example.projectjee.dto.PeriodSalesDTO;
import org.example.projectjee.services.VenteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateur")
@CrossOrigin(origins = "http://localhost:3000") // adapte si besoin
public class VenteController {

    private final VenteService venteService;

    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    @GetMapping("/ventes-par-categorie")
    public List<PeriodSalesDTO> ventesParCategorie(
            @RequestParam(defaultValue = "mois") String periode
    ) {
        return venteService.getVentesParCategorie(periode);
    }
}
