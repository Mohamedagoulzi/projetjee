package org.example.projectjee.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.example.projectjee.dto.AdminDashboardReportDTO;
import org.example.projectjee.dto.ProduitCategorieStatDTO;
import org.example.projectjee.dto.TopProduitDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.List;

// ✅ important: on utilise java.awt.Color (OpenPDF l’accepte)
// ⚠️ mais on n’importe PAS java.awt.* pour éviter conflits
import java.awt.Color;

@Service
public class AdminDashboardPdfService {

    private static final Color PURPLE = new Color(108, 77, 245);
    private static final Color LIGHT_BG = new Color(248, 246, 255);
    private static final Color SOFT_GRAY = new Color(120, 120, 120);
    private static final Color BORDER_LIGHT = new Color(230, 225, 255);
    private static final Color BORDER_TABLE = new Color(230, 230, 230);

    private static final DecimalFormat DF2 = new DecimalFormat("0.00");

    public byte[] generate(AdminDashboardReportDTO report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 56, 56, 56, 56);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ✅ Fonts (OpenPDF)
            Font title = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, Color.BLACK);
            Font h2    = FontFactory.getFont(FontFactory.HELVETICA, 13, Font.BOLD, Color.BLACK);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK);
            Font muted  = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, SOFT_GRAY);
            Font badge  = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, PURPLE);

            // ===== Title
            Paragraph pTitle = new Paragraph("Rapport Dashboard Admin", title);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitle);
            doc.add(Chunk.NEWLINE);

            // ===== Identity Card
            PdfPTable id = new PdfPTable(1);
            id.setWidthPercentage(100);

            PdfPCell box = new PdfPCell();
            box.setBackgroundColor(LIGHT_BG);
            box.setBorderColor(BORDER_LIGHT);
            box.setPadding(12);

            Paragraph idTitle = new Paragraph("Identifiant du rapport", h2);
            idTitle.setSpacingAfter(6);
            box.addElement(idTitle);

            box.addElement(new Paragraph("Nom : " + safe(report.getAdminNom()), normal));
            box.addElement(new Paragraph("Email : " + safe(report.getAdminEmail()), normal));
            box.addElement(new Paragraph("Date : " + safe(report.getDate()), normal));

            id.addCell(box);
            doc.add(id);
            doc.add(Chunk.NEWLINE);

            // ===== Summary
            Paragraph sH = new Paragraph("Résumé", h2);
            sH.setSpacingAfter(6);
            doc.add(sH);

            Paragraph s = new Paragraph(safe(report.getResume()), normal);
            s.setLeading(16);
            doc.add(s);
            doc.add(Chunk.NEWLINE);

            // ===== KPI cards
            PdfPTable kpi = new PdfPTable(2);
            kpi.setWidthPercentage(100);
            kpi.setSpacingBefore(4);
            kpi.setSpacingAfter(10);
            kpi.setWidths(new float[]{1, 1});

            kpi.addCell(kpiCell("Total produits", String.valueOf(nvl(report.getTotalProducts())), badge));
            kpi.addCell(kpiCell("Total catégories", String.valueOf(nvl(report.getTotalCategories())), badge));
            kpi.addCell(kpiCell("Note moyenne globale", DF2.format(nvlD(report.getNoteMoyenneGlobale())), badge));
            kpi.addCell(kpiCell("Catégorie la plus commandée",
                    safe(report.getTopCategorieNom()) + " (" + nvl(report.getTopCategorieQuantite()) + ")", badge));

            doc.add(kpi);

            // ===== Distribution par catégorie
            Paragraph distH = new Paragraph("Distribution des produits par catégorie", h2);
            distH.setSpacingAfter(6);
            doc.add(distH);

            PdfPTable dist = new PdfPTable(3);
            dist.setWidthPercentage(100);
            dist.setWidths(new float[]{2.2f, 1f, 1f});

            addHeader(dist, "Catégorie");
            addHeader(dist, "Nb produits");
            addHeader(dist, "%");

            List<ProduitCategorieStatDTO> distData = report.getProduitsParCategorie();
            long total = 0;
            if (distData != null) for (ProduitCategorieStatDTO d : distData) total += (d == null ? 0 : nvl(d.getTotalProduits()));

            if (distData == null || distData.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Aucune donnée", muted));
                empty.setColspan(3);
                empty.setPadding(8);
                empty.setBorderColor(BORDER_TABLE);
                dist.addCell(empty);
            } else {
                for (ProduitCategorieStatDTO d : distData) {
                    if (d == null) continue;
                    long nb = nvl(d.getTotalProduits());
                    double pct = total == 0 ? 0 : (nb * 100.0 / total);

                    dist.addCell(bodyCell(safe(d.getCategorieNom())));
                    dist.addCell(bodyCell(String.valueOf(nb)));
                    dist.addCell(bodyCell(DF2.format(pct) + " %"));
                }
            }

            doc.add(dist);
            doc.add(Chunk.NEWLINE);

            // ===== Top 10 produits
            Paragraph topH = new Paragraph("Top 10 Produits", h2);
            topH.setSpacingAfter(6);
            doc.add(topH);

            PdfPTable top = new PdfPTable(6);
            top.setWidthPercentage(100);
            top.setWidths(new float[]{0.6f, 2.6f, 1.4f, 1.0f, 0.9f, 0.9f});

            addHeader(top, "#");
            addHeader(top, "Produit");
            addHeader(top, "Catégorie");
            addHeader(top, "Prix");
            addHeader(top, "Note");
            addHeader(top, "Avis");

            List<TopProduitDTO> topList = report.getTopProduits();
            if (topList == null || topList.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Aucun produit", muted));
                empty.setColspan(6);
                empty.setPadding(8);
                empty.setBorderColor(BORDER_TABLE);
                top.addCell(empty);
            } else {
                int i = 1;
                for (TopProduitDTO p : topList) {
                    if (p == null) continue;
                    top.addCell(bodyCell(String.valueOf(i++)));
                    top.addCell(bodyCell(safe(p.getNom())));
                    top.addCell(bodyCell(safe(p.getCategorieNom())));
                    top.addCell(bodyCell(nvlPrice(p.getPrix())));
                    top.addCell(bodyCell(nvlNote(p.getNoteMoyenne())));
                    top.addCell(bodyCell(String.valueOf(nvlInt(p.getNombreReviews()))));
                }
            }

            doc.add(top);

            // ===== Footer
            doc.add(Chunk.NEWLINE);
            Paragraph foot = new Paragraph("Généré automatiquement par la plateforme.", muted);
            foot.setAlignment(Element.ALIGN_RIGHT);
            doc.add(foot);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }
    }

    private PdfPCell kpiCell(String label, String value, Font valueFont) {
        Font lbl = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, SOFT_GRAY);
        Font val = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD,
                valueFont != null ? valueFont.getColor() : Color.BLACK);

        PdfPCell c = new PdfPCell();
        c.setPadding(12);
        c.setBackgroundColor(LIGHT_BG);
        c.setBorderColor(BORDER_LIGHT);

        Paragraph p1 = new Paragraph(label, lbl);
        Paragraph p2 = new Paragraph(value, val);
        p2.setSpacingBefore(4);

        c.addElement(p1);
        c.addElement(p2);
        return c;
    }

    private void addHeader(PdfPTable t, String text) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(PURPLE);
        c.setPadding(7);
        c.setBorderColor(PURPLE);
        t.addCell(c);
    }

    private PdfPCell bodyCell(String text) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(7);
        c.setBorderColor(BORDER_TABLE);
        return c;
    }

    private String safe(String s) { return s == null ? "" : s; }
    private long nvl(Long v) { return v == null ? 0L : v; }
    private double nvlD(Double v) { return v == null ? 0.0 : v; }
    private int nvlInt(Integer v) { return v == null ? 0 : v; }

    private String nvlPrice(Object prix) {
        if (prix == null) return "-";
        return String.valueOf(prix) + " DH";
    }

    private String nvlNote(Object note) {
        if (note == null) return "-";
        try {
            double d = Double.parseDouble(String.valueOf(note));
            return DF2.format(d);
        } catch (Exception e) {
            return String.valueOf(note);
        }
    }
}
