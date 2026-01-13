package org.example.projectjee.controllers;

import java.util.List;

import org.example.projectjee.model.Product;
import org.example.projectjee.services.ProductExportService;
import org.example.projectjee.services.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/export")
@CrossOrigin(origins = "http://localhost:3000") // adapte le port
public class ProductExportController {

    private final ProductService productService;
    private final ProductExportService exportService;

    public ProductExportController(ProductService productService, ProductExportService exportService) {
        this.productService = productService;
        this.exportService = exportService;
    }

    @GetMapping("/products.csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<Product> products = productService.findAll();
        byte[] data = exportService.exportCsv(products);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    @GetMapping("/products.xlsx")
    public ResponseEntity<byte[]> exportExcel() {
        List<Product> products = productService.findAll();
        byte[] data = exportService.exportExcel(products);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(data);
    }
}
