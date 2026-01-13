package org.example.projectjee.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

 import org.example.projectjee.dto.CategoryAmountDTO;
import org.example.projectjee.dto.PeriodSalesDTO;
import org.example.projectjee.dto.TopCategorieDTO;
import org.example.projectjee.repository.VenteRepository;
import org.springframework.stereotype.Service;

@Service
public class VenteService {

    private final VenteRepository venteRepository;

    public VenteService(VenteRepository venteRepository) {
        this.venteRepository = venteRepository;
    }

    public List<PeriodSalesDTO> getVentesParCategorie(String periode) {

        List<Object[]> rows =
                venteRepository.findSalesGroupedByPeriodAndCategory(periode);

        Map<String, List<CategoryAmountDTO>> grouped = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String label   = (String) row[0];                 // period_label
            String catName = (String) row[1];                 // category_name
            Double amount  = ((Number) row[2]).doubleValue(); // total_amount

            grouped
                .computeIfAbsent(label, k -> new ArrayList<>())
                .add(new CategoryAmountDTO(catName, amount));
        }

        List<PeriodSalesDTO> result = new ArrayList<>();
        grouped.forEach((label, cats) -> result.add(new PeriodSalesDTO(label, cats)));

        return result;
    }


   

    public TopCategorieDTO getTopCategorie() {
        List<Object[]> rows = venteRepository.findTopCategorie();

        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return new TopCategorieDTO("—", 0);
        }

        Object[] r = rows.get(0);

        String categorieNom = (r[0] != null) ? r[0].toString() : "—";
        long totalQte = (r[1] != null) ? ((Number) r[1]).longValue() : 0L;

        return new TopCategorieDTO(categorieNom, totalQte);
    }

}
