package org.example.projectjee.dto;

import java.util.List;

public class PeriodSalesDTO {
    private String label;
    private List<CategoryAmountDTO> categories;

    public PeriodSalesDTO(String label, List<CategoryAmountDTO> categories) {
        this.label = label;
        this.categories = categories;
    }

    public String getLabel() {
        return label;
    }

    public List<CategoryAmountDTO> getCategories() {
        return categories;
    }
}
