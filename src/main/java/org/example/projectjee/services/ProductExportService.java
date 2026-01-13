package org.example.projectjee.services;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.projectjee.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductExportService {

    // ✅ CSV
    public byte[] exportCsv(List<Product> products) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("id,titre,prix,note_moyenne,nombre_avis,categorie\n");

        for (Product p : products) {
            sb.append(csv(p.getId()))
            .append(",").append(csv(p.getTitle()))
            .append(",").append(csv(p.getPrice()))
            .append(",").append(csv(p.getRating()))
            .append(",").append(csv(p.getRatingCount()))

              .append(",").append(csv(p.getCategorie() != null ? p.getCategorie().getNom() : ""))
              .append("\n");
        }

        // (optionnel) BOM UTF-8 pour Excel Windows
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);
        return out;
    }

    private String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        // escape: " -> ""
        s = s.replace("\"", "\"\"");
        // si contient virgule / guillemet / saut de ligne => entourer par ""
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = "\"" + s + "\"";
        }
        return s;
    }

    // ✅ EXCEL (.xlsx)
    public byte[] exportExcel(List<Product> products) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Produits");

            // style header
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Titre", "Prix", "Note", "Reviews", "Catégorie"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            int r = 1;
            for (Product p : products) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(p.getId() != null ? p.getId() : 0);
                row.createCell(1).setCellValue(p.getTitle() != null ? p.getTitle() : "");
                row.createCell(2).setCellValue(p.getPrice() != null ? p.getPrice() : 0);
                row.createCell(3).setCellValue(p.getRating() != null ? p.getRating() : 0);
                row.createCell(4).setCellValue(p.getRatingCount() != null ? p.getRatingCount() : 0);

                row.createCell(5).setCellValue(
                        p.getCategorie() != null && p.getCategorie().getNom() != null
                                ? p.getCategorie().getNom()
                                : ""
                );
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur export Excel", e);
        }
    }
}
