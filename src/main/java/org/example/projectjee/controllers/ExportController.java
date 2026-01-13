package org.example.projectjee.controllers;

import java.util.List;

import org.example.projectjee.model.Product;
import org.example.projectjee.repository.ProduitRepository;
import org.example.projectjee.services.ProductExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/utilisateur/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ExportController {

    private final ProduitRepository produitRepository;
    private final ProductExportService exportService;

    @GetMapping(value = "/products.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<Product> products = produitRepository.findAll();

        byte[] file = exportService.exportCsv(products);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(file);
    }

    @GetMapping(value = "/products.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel() {
        List<Product> products = produitRepository.findAll();

        byte[] file = exportService.exportExcel(products);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
            .body(file);
    }
}
