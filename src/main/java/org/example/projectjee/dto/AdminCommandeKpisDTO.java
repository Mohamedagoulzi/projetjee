package org.example.projectjee.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCommandeKpisDTO {
    private long confirmedOrders;
    private long pendingCarts;
    private double confirmedTotal;
    private double pendingTotal;
}
