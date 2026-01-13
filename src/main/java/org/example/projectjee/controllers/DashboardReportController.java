package org.example.projectjee.controllers;

import org.example.projectjee.dto.AdminDashboardReportDTO;
import org.example.projectjee.services.AdminDashboardPdfService;
import org.example.projectjee.services.AdminDashboardReportService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateur")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardReportController {

    private final AdminDashboardReportService reportService;
    private final AdminDashboardPdfService pdfService;

    public DashboardReportController(AdminDashboardReportService reportService,
                                     AdminDashboardPdfService pdfService) {
        this.reportService = reportService;
        this.pdfService = pdfService;
    }

    @GetMapping(value = "/dashboard-report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(Authentication auth) {

        // ✅ sécurité
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AdminDashboardReportDTO report = reportService.buildReport(auth);
        byte[] pdf = pdfService.generate(report);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-report.pdf")
                .body(pdf);
    }
}
