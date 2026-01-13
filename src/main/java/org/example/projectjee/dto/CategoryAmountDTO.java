package org.example.projectjee.dto;

public class CategoryAmountDTO {
    private String name;
    private double amount;

    public CategoryAmountDTO(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }
}
